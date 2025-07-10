package com.codewise.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult { // AI 분석 결과(점수, 요약 등)를 저장하는 엔티티 클래스

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 분석 결과 고유 식별자 (ID)

    @OneToOne
    private CodeSubmission codeSubmission;

    private double maintainabilityScore; // 코드 유지보수성 점수
    private double readabilityScore;  // 코드 가독성 점수
    private double bugProbability; // 코드에 버그가 있을 확률

    @Column(columnDefinition = "TEXT")
    private String summary; // 분석 결과에 대한 요약 설명

    @Column(columnDefinition = "TEXT")
    private String suggestions; // 코드 개선을 위한 제안 사항

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 분석 결과가 생성된 시간

    @Column(name = "score")
    private Integer score;// 전반적인 코드 품질 점수

}
