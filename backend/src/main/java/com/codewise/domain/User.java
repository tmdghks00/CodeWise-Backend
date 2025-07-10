package com.codewise.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User { // 사용자 정보(이메일, 비밀번호, 역할 등)를 저장하는 엔티티 클래스

    @Id // 기본 키 임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 고유 식별자 (ID)

    @Column(nullable = false, unique = true) // 데이터베이스 컬럼과 매핑. null 을 허용하지 않으며, 값은 유일해야 함
    private String email; // 사용자 이메일 (로그인 ID로 사용될 수 있음)

    @Column(nullable = false) // 데이터베이스 컬럼과 매핑. null 을 허용하지 않음
    private String password;  // 사용자 비밀번호

    @Enumerated(EnumType.STRING)
    private UserRole role; // 사용자 권한 (USER, ADMIN)

}
