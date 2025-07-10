package com.codewise.dto;

import com.codewise.domain.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisResultDto { // 클라이언트에 전달할 분석 결과 데이터를 담는 DTO 클래스
    private Long id;                     // 분석 결과의 고유 식별자
    private double maintainabilityScore; // 코드 유지보수성 점수
    private double readabilityScore;     // 코드 가독성 점수
    private double bugProbability;       // 코드 버그 발생 확률
    private String summary;              // 분석 결과 요약
    private String suggestions;          // 코드 개선 제안 사항

    public static AnalysisResultDto fromEntity(AnalysisResult result) {
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


