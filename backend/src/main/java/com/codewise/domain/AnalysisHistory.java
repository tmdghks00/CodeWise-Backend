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
@Table(name = "analysis_history")
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String language;
    private String purpose;
    private String errorType;
    private String errorMessage;
    private LocalDateTime createdAt;

    @Column(length = 100, unique = false)
    private String idempotencyKey; // 중복 저장 방지용 키
}
