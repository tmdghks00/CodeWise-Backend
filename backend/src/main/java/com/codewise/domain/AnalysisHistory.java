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

    // JWT에서 식별한 사용자 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String language;      // 예: "java"
    private String purpose;       // 예: "security_hardening"
    private String errorType;     // 예: "error" | "warn" | "info"
    private String errorMessage;  // 예: "NPE 가능성"
    private LocalDateTime createdAt;
}
