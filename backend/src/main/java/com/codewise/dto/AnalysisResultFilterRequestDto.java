package com.codewise.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisResultFilterRequestDto { // 분석 이력 필터링 및 정렬 요청 시 사용하는 DTO 클래스
    private String sortBy;   // 정렬 기준 ("date", "score" 등)
    private String order;    // 정렬 방향 ("asc" 오름차순, "desc" 내림차순)
    private String language; // (선택 사항) 특정 프로그래밍 언어로 필터링
    private String keyword;  // (선택 사항) 코드 내용에 포함된 키워드로 검색 필터링
}
