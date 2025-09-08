package com.codewise.service;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.domain.UserRole;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.dto.AnalysisResultFilterRequestDto;
import com.codewise.dto.AnalyzeResponse;
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

    public void saveNewResult(String email, String code, String language, AnalyzeResponse aiResponse) {
        // 1. 사용자 조회 → 없으면 자동 등록
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.warn("⚠️ [AnalysisResultService] 사용자 없음 → 자동 등록 진행: {}", email);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(new BCryptPasswordEncoder().encode("test1234")); // fallback 기본 패스워드
            newUser.setRole(UserRole.USER);
            return userRepository.save(newUser);
        });

        // 2. 새 CodeSubmission 생성 및 저장
        CodeSubmission submission = CodeSubmission.builder()
                .user(user)
                .code(code)
                .language(language != null ? language : "auto")
                .build();
        codeSubmissionRepository.save(submission);

        // 3. AI 응답 기반 AnalysisResult 생성 및 저장
        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(submission)
                .maintainabilityScore((Double) aiResponse.metrics().get("maintainability"))
                .readabilityScore((Double) aiResponse.metrics().get("readability"))
                .bugProbability((Double) aiResponse.metrics().get("bug_probability"))
                .summary(aiResponse.summary())
                .suggestions(aiResponse.issues().toString())
                .score((Integer) aiResponse.metrics().get("score"))
                .createdAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(analysisResult);
        log.info("✅ [AnalysisResultService] 결과 저장 완료. email={}, submissionId={}", email, submission.getId());
    }

    public void saveResultFromAiResponse(Long submissionId, AnalyzeResponse aiResponse) {
        CodeSubmission codeSubmission = codeSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드를 찾을 수 없습니다."));

        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(codeSubmission)
                .maintainabilityScore((Double) aiResponse.metrics().get("maintainability"))
                .readabilityScore((Double) aiResponse.metrics().get("readability"))
                .bugProbability((Double) aiResponse.metrics().get("bug_probability"))
                .summary(aiResponse.summary())
                .suggestions(aiResponse.issues().toString())
                .score((Integer) aiResponse.metrics().get("score"))
                .createdAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(analysisResult);
    }

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

        List<AnalysisResult> results = analysisResultRepository.findAllByCodeSubmission_User(user);
        return results.stream()
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
        } else if ("maintainability".equalsIgnoreCase(filterDto.getSortBy())) {
            comparator = Comparator.comparing(AnalysisResult::getMaintainabilityScore);
        } else if ("readability".equalsIgnoreCase(filterDto.getSortBy())) {
            comparator = Comparator.comparing(AnalysisResult::getReadabilityScore);
        } else if ("bug".equalsIgnoreCase(filterDto.getSortBy())) {
            comparator = Comparator.comparing(AnalysisResult::getBugProbability);
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
}
