package com.codewise.service;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.dto.AnalysisResultFilterRequestDto;
import com.codewise.dto.AnalyzeResponse;
import com.codewise.repository.AnalysisResultRepository;
import com.codewise.repository.CodeSubmissionRepository;
import com.codewise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 변경 요약
 * 1. 자동 계정 생성 로직 제거 (랜덤 유저 생성 X)
 * 2. score, bugProbability, readabilityScore 관련 필드 삭제 (AnalysisResult 엔티티와 일치)
 * 3. email 기반 사용자 조회로 변경
 * 4. 코드 전반 정리 및 주석 추가
 */
@Service
@RequiredArgsConstructor
public class AnalysisResultService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisResultService.class);

    private final AnalysisResultRepository analysisResultRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;

    // 안전한 숫자 변환
    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number num) return num.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 새 분석 결과 저장 (AI 분석 결과를 DB에 기록)
     * - 기존의 랜덤 유저 자동 생성 로직 제거
     */
    public void saveNewResult(String email, String code, String language, String purpose, AnalyzeResponse aiResponse) { // ✅ purpose 필드 추가
        // 이메일 기반으로 사용자 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 코드 제출 기록 생성
        CodeSubmission submission = CodeSubmission.builder()
                .user(user)
                .code(code)
                .language(language != null ? language : "auto")
                .purpose(purpose)
                .build();
        codeSubmissionRepository.save(submission);

        // score, bugProbability 관련 항목 제거 완료
        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(submission)
                .language(language)
                .purpose(purpose)
                .summary(aiResponse.summary())
                .suggestions(aiResponse.issues() != null ? aiResponse.issues().toString() : "")
                .createdAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(analysisResult);
        log.info("분석 결과 저장 완료 for {}", email);
    }

    /**
     * AI 응답 기반 결과 저장 (submissionId 기준)
     */
    public void saveResultFromAiResponse(Long submissionId, AnalyzeResponse aiResponse) {
        CodeSubmission codeSubmission = codeSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드를 찾을 수 없습니다."));

        // 제거: maintainabilityScore, readabilityScore, bugProbability, score
        AnalysisResult analysisResult = AnalysisResult.builder()
                .codeSubmission(codeSubmission)
                .language(codeSubmission.getLanguage())
                .purpose(aiResponse.purpose())
                .summary(aiResponse.summary())
                .suggestions(aiResponse.issues() != null ? aiResponse.issues().toString() : "")
                .createdAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(analysisResult);
        log.info("submissionId={} 분석 결과 저장 완료", submissionId);
    }

    /**
     * 제출 ID 기반 결과 조회
     */
    public AnalysisResultDto getResultBySubmissionId(Long id) {
        CodeSubmission sub = codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제출 코드 없음"));
        AnalysisResult result = analysisResultRepository.findByCodeSubmission(sub)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과 없음"));
        return AnalysisResultDto.fromEntity(result);
    }

    /**
     * 제출 ID + 유저 ID 기반 결과 조회
     */
    public AnalysisResultDto getResultBySubmissionIdAndUserId(Long submissionId, Long userId) {
        AnalysisResult result = analysisResultRepository
                .findByCodeSubmission_IdAndCodeSubmission_User_Id(submissionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과 없음"));
        return AnalysisResultDto.fromEntity(result);
    }

    /**
     * 전체 결과 (마이페이지용)
     */
    public List<AnalysisResultDto> getAllResultsForUser(String email) {
        return analysisResultRepository.findAllByCodeSubmission_User_Email(email).stream()
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 분석 이력 전체 조회
     */
    public List<AnalysisResultDto> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisResult> results = analysisResultRepository.findAllByCodeSubmission_User(user);
        return results.stream()
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 필터 및 정렬 기반 분석 이력 조회
     * - score 등 제거로 관련 comparator 수정됨
     */
    public List<AnalysisResultDto> getFilteredAndSortedUserHistory(String email, AnalysisResultFilterRequestDto filterDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisResult> results = analysisResultRepository.findByCodeSubmission_User_Id(user.getId());

        // 키워드 필터
        if (filterDto.getKeyword() != null && !filterDto.getKeyword().isBlank()) {
            results = results.stream()
                    .filter(r -> r.getSummary() != null && r.getSummary().contains(filterDto.getKeyword()))
                    .collect(Collectors.toList());
        }

        // score 제거됨 → createdAt 기준 정렬만 유지
        Comparator<AnalysisResult> comparator = Comparator.comparing(AnalysisResult::getCreatedAt);
        if ("desc".equalsIgnoreCase(filterDto.getOrder())) {
            comparator = comparator.reversed();
        }

        return results.stream()
                .sorted(comparator)
                .map(AnalysisResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 분석 ID 기반 단일 조회
     */
    public AnalysisResultDto getAnalysisById(Long id) {
        AnalysisResult result = analysisResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 분석 결과를 찾을 수 없습니다."));
        return AnalysisResultDto.fromEntity(result);
    }
}
