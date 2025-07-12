package com.codewise.repository;

import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 사용자(User) 정보를 DB 에서 조회하고 관리하기 위한 JPA 리포지토리 인터페이스
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // Google 소셜 로그인 추가 메서드
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
