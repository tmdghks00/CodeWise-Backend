package com.codewise.dto;

import com.codewise.domain.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisResultDto {
    private Long id;                     // 분석 결과 고유 식별자
    private Long submissionId;           // 코드 제출 ID
    private Long userId;                 // 사용자 ID
    private String email;                // 사용자 이메일
    private String summary;              // 분석 결과 요약
    private String suggestions;          // 코드 개선 제안 사항

    public static AnalysisResultDto fromEntity(AnalysisResult result) {
        return new AnalysisResultDto(
                result.getId(),
                result.getCodeSubmission() != null ? result.getCodeSubmission().getId() : null,
                result.getCodeSubmission() != null && result.getCodeSubmission().getUser() != null
                        ? result.getCodeSubmission().getUser().getId() : null,
                result.getCodeSubmission() != null && result.getCodeSubmission().getUser() != null
                        ? result.getCodeSubmission().getUser().getEmail() : null, // ✅ 추가된 부분
                result.getSummary(),
                result.getSuggestions()
        );
    }
}
