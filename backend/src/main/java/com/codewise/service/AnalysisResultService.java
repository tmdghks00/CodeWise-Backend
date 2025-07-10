package com.codewise.service;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.dto.AnalysisResultFilterRequestDto; // 필터링 DTO 임포트
import com.codewise.exception.CustomException;
import com.codewise.repository.AnalysisResultRepository;
import com.codewise.repository.CodeSubmissionRepository;
import com.codewise.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode; // JsonNode 임포트
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // LocalDateTime 임포트 (createdAt 설정을 위함)
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisResultService {
    // 분석 결과 관련 비즈니스 로직을 처리하는 서비스 클래스

    private final AnalysisResultRepository analysisResultRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;
    private final AiServerClient aiServerClient; // AI 서버 클라이언트

    public AnalysisResultService(AnalysisResultRepository analysisResultRepository,
                                 CodeSubmissionRepository codeSubmissionRepository,
                                 UserRepository userRepository,
                                 AiServerClient aiServerClient) {
        this.analysisResultRepository = analysisResultRepository;
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.userRepository = userRepository;
        this.aiServerClient = aiServerClient;
    }

    // 특정 코드 제출 ID에 대한 분석 결과를 조회하는 메서드
    public AnalysisResultDto getResultBySubmissionId(Long id) {
        CodeSubmission sub = codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드 없음"));
        AnalysisResult result = analysisResultRepository.findByCodeSubmission(sub)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과 없음"));
        return toDto(result);
    }

    // 특정 사용자 이메일로 모든 분석 결과를 조회하는 메서드 (기존 유지)
    public List<AnalysisResultDto> getAllResultsForUser(String email) {
        return analysisResultRepository.findAllByCodeSubmission_User_Email(email).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 실시간 코드 분석을 요청하고, 결과를 파싱하여 DB에 저장한 후 DTO를 반환합니다.
     * 이 메서드는 WebSocket 요청에 의해 호출됩니다.
     * @param username 분석을 요청한 사용자 이름 (이메일)
     * @param code 분석할 코드 내용
     * @return 저장된 AnalysisResult의 DTO
     */
    public AnalysisResultDto analyzeAndSaveCodeRealtime(String username, String code) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        // 1. AI 서버에 코드 분석 요청
        // 현재는 언어가 고정되어 있지 않으므로, WebSocketRequestDto 에서 language 필드를 받아와 AiServerClient 로 넘기거나,
        // AI 서버가 언어를 자동 감지한다고 가정해야 합니다. 여기서는 편의상 code 만 넘깁니다.
        JsonNode aiResultJson = aiServerClient.analyzeCode(username, code);

        // 2. 새로운 CodeSubmission 엔티티 생성 및 저장
        // 실시간 분석 요청이 들어올 때마다 새로운 CodeSubmission을 저장하는 것을 가정
        CodeSubmission newSubmission = CodeSubmission.builder()
                .user(user)
                .code(code)
                .language("Unknown") // 또는 WebSocketRequestDto에서 받은 language 사용
                .submittedAt(LocalDateTime.now())
                .build();
        newSubmission = codeSubmissionRepository.save(newSubmission); // 저장 후 ID가 부여된 엔티티 반환

        // 3. AI 분석 결과 (JsonNode) 파싱 및 AnalysisResult 엔티티 생성
        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(newSubmission) // 새로 저장된 CodeSubmission 연결
                // AI 서버 응답의 구조에 따라 JsonNode 에서 값 추출
                // 예시: AI 서버가 {"maintainability": 0.8, "readability": 0.7, "bug": 0.1, "summary": "...", "suggestions": "...", "score": 90} 형태로 응답한다고 가정
                .maintainabilityScore(aiResultJson.path("maintainability").asDouble(0.0))
                .readabilityScore(aiResultJson.path("readability").asDouble(0.0))
                .bugProbability(aiResultJson.path("bug").asDouble(0.0))
                .summary(aiResultJson.path("summary").asText("No summary"))
                .suggestions(aiResultJson.path("suggestions").asText("No suggestions"))
                .score(aiResultJson.path("score").asInt(0))
                .createdAt(LocalDateTime.now()) // 분석 결과 저장 시간
                .build();

        // 4. AnalysisResult 엔티티 저장
        analysisResult = analysisResultRepository.save(analysisResult);

        // 5. 저장된 AnalysisResult 를 DTO 로 변환하여 반환
        return toDto(analysisResult);
    }

    // 특정 사용자 이메일로 분석 이력을 조회하는 메서드 (기존 유지)
    public List<AnalysisResultDto> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        List<AnalysisResult> results = analysisResultRepository.findAllByCodeSubmission_User(user);
        return results.stream()
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 분석 이력을 필터링 및 정렬하여 조회하는 메서드 (AnalysisResultServiceImpl의 기능 통합)
     * @param email 사용자 이메일
     * @param filterDto 필터링 및 정렬 기준을 담은 DTO
     * @return 필터링 및 정렬된 분석 결과 DTO 리스트
     */
    public List<AnalysisResultDto> getFilteredAndSortedUserHistory(String email, AnalysisResultFilterRequestDto filterDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다."));

        // Repository 에서 사용자 ID 기반으로 결과 조회 (AnalysisResultRepository 의 findByCodeSubmission_User_Id 사용 권장)
        List<AnalysisResult> results = analysisResultRepository.findByCodeSubmission_User_Id(user.getId());

        // 1. 필터링 로직
        if (filterDto.getKeyword() != null && !filterDto.getKeyword().isBlank()) {
            results = results.stream()
                    .filter(r -> r.getSummary() != null && r.getSummary().contains(filterDto.getKeyword()))
                    .collect(Collectors.toList());
        }

        // 2. 정렬 로직
        Comparator<AnalysisResult> comparator = Comparator.comparing(AnalysisResult::getCreatedAt); // 기본: 생성 시간
        if ("score".equalsIgnoreCase(filterDto.getSortBy())) {
            comparator = Comparator.comparing(AnalysisResult::getScore);
        } else if ("maintainability".equalsIgnoreCase(filterDto.getSortBy())) { // 추가: 유지보수성으로 정렬
            comparator = Comparator.comparing(AnalysisResult::getMaintainabilityScore);
        } else if ("readability".equalsIgnoreCase(filterDto.getSortBy())) { // 추가: 가독성으로 정렬
            comparator = Comparator.comparing(AnalysisResult::getReadabilityScore);
        } else if ("bug".equalsIgnoreCase(filterDto.getSortBy())) { // 추가: 버그 확률로 정렬
            comparator = Comparator.comparing(AnalysisResult::getBugProbability);
        }
        // "id" 정렬은 기본값으로 처리됩니다.

        if ("desc".equalsIgnoreCase(filterDto.getOrder())) { // 정렬 방향이 "desc"이면 역순
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator)
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }


    public AnalysisResultDto getAnalysisById(Long id) { // 특정 ID의 분석 결과를 조회하는 메서드 (기존 유지)
        AnalysisResult result = analysisResultRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 분석 결과를 찾을 수 없습니다."));
        return AnalysisResultDto.fromEntity(result);
    }

    private AnalysisResultDto toDto(AnalysisResult result) { // AnalysisResult 엔티티를 DTO로 변환하는 헬퍼 메서드 (기존 유지)
        return new AnalysisResultDto(
                result.getId(),
                result.getMaintainabilityScore(),
                result.getReadabilityScore(),
                result.getBugProbability(),
                result.getSummary(),
                result.getSuggestions()
        );
    }
}