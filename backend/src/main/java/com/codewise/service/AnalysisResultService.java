package com.codewise.service;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.domain.UserRole;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.dto.AnalysisResultFilterRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.codewise.repository.AnalysisResultRepository;
import com.codewise.repository.CodeSubmissionRepository;
import com.codewise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisResultService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisResultService.class);

    private final AnalysisResultRepository analysisResultRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();   // ✅ ObjectMapper 주입

    /** 숫자 변환 Utility */
    private Double toDouble(JsonNode node) {
        if (node == null || node.isNull()) return 0.0;
        return node.asDouble(0.0);
    }

    private Integer toInt(JsonNode node) {
        if (node == null || node.isNull()) return 0;
        return node.asInt(0);
    }


    /**
     * ✅ STOMP Websocket 분석 결과 저장
     * → JsonNode로 받아서 저장 / DTO 매핑 제거
     */
    public void saveNewResult(String email, String code, String language, String aiResponseJson) {
        try {
            JsonNode root = objectMapper.readTree(aiResponseJson);

            JsonNode metrics = root.path("metrics");
            JsonNode issues = root.path("issues");
            JsonNode fix = root.path("fix");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(new BCryptPasswordEncoder().encode("test1234"));
                newUser.setRole(UserRole.USER);
                return userRepository.save(newUser);
            });

            CodeSubmission submission = CodeSubmission.builder()
                    .user(user)
                    .code(code)
                    .language(language != null ? language : "auto")
                    .build();
            codeSubmissionRepository.save(submission);

            AnalysisResult analysisResult = AnalysisResult.builder()
                    .codeSubmission(submission)
                    .maintainabilityScore(toDouble(metrics.path("maintainability")))
                    .readabilityScore(toDouble(metrics.path("readability")))
                    .bugProbability(toDouble(metrics.path("bug_probability")))
                    .summary(root.path("summary").asText(""))
                    .suggestions(issues.toString())          // ✅ 전체 JSON 문자열 저장
                    .score(toInt(metrics.path("score")))
                    .createdAt(LocalDateTime.now())
                    .build();

            analysisResultRepository.save(analysisResult);

            log.info("✅ 분석 결과 저장 완료 (email = {}, score = {})", email, toInt(metrics.path("score")));

        } catch (Exception e) {
            log.error("❌ AI JSON 저장 실패: {}", aiResponseJson, e);
            throw new RuntimeException("AI Response 저장 중 오류 발생", e);
        }
    }


    /** 기존 제출 기반 저장 방식 (REST) */
    public void saveResultFromAiResponse(Long submissionId, JsonNode aiResponseNode) {
        CodeSubmission codeSubmission = codeSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드를 찾을 수 없습니다."));

        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(codeSubmission)
                .maintainabilityScore(toDouble(aiResponseNode.path("metrics").path("maintainability")))
                .readabilityScore(toDouble(aiResponseNode.path("metrics").path("readability")))
                .bugProbability(toDouble(aiResponseNode.path("metrics").path("bug_probability")))
                .summary(aiResponseNode.path("summary").asText(""))
                .suggestions(aiResponseNode.path("issues").toString())
                .score(toInt(aiResponseNode.path("metrics").path("score")))
                .createdAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(analysisResult);
    }


    /** 나머지 메서드는 기존 코드 그대로 유지 */
    public AnalysisResultDto getResultBySubmissionId(Long id) {
        CodeSubmission sub = codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드 없음"));
        AnalysisResult result = analysisResultRepository.findByCodeSubmission(sub)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과 없음"));
        return AnalysisResultDto.fromEntity(result);
    }

    public List<AnalysisResultDto> getAllResultsForUser(String email) {
        return analysisResultRepository.findAllByCodeSubmission_User_Email(email).stream()
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AnalysisResultDto> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return analysisResultRepository.findAllByCodeSubmission_User(user).stream()
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AnalysisResultDto> getFilteredAndSortedUserHistory(String email, AnalysisResultFilterRequestDto filterDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisResult> results = analysisResultRepository.findByCodeSubmission_User_Id(user.getId());

        if (filterDto.getKeyword() != null && !filterDto.getKeyword().isBlank()) {
            results = results.stream()
                    .filter(r -> r.getSummary() != null && r.getSummary().contains(filterDto.getKeyword()))
                    .collect(Collectors.toList());
        }

        Comparator<AnalysisResult> comparator = Comparator.comparing(AnalysisResult::getCreatedAt);
        if ("score".equalsIgnoreCase(filterDto.getSortBy())) {
            comparator = Comparator.comparing(AnalysisResult::getScore);
        }

        if ("desc".equalsIgnoreCase(filterDto.getOrder())) {
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator)
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AnalysisResultDto getAnalysisById(Long id) {
        AnalysisResult result = analysisResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));
        return AnalysisResultDto.fromEntity(result);
    }

    /** submissionId + userId 조합 조회 */
    public AnalysisResultDto getResultBySubmissionIdAndUserId(Long submissionId, Long userId) {
        AnalysisResult result = analysisResultRepository
                .findByCodeSubmission_IdAndCodeSubmission_User_Id(submissionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 submissionId + userId 의 분석 결과가 없습니다."
                ));

        return AnalysisResultDto.fromEntity(result);
    }

}
