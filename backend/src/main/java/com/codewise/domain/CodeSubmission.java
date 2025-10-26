package com.codewise.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSubmission { // 사용자가 제출한 코드 정보를 저장하는 엔티티 클래스

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 코드 제출 고유 식별자 (ID)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 코드를 제출한 사용자 엔티티

    @Column(columnDefinition = "TEXT")
    private String code; // 사용자가 제출한 실제 코드 내용

    private String language; // 제출된 코드의 프로그래밍 언어 (예: "Java", "Python")

    private String purpose; // 추가: 코드 제출 목적

    private LocalDateTime submittedAt;

}
