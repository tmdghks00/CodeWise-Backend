package com.codewise.repository;

import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// CodeSubmission 데이터를 DB에 저장하고 검색하기 위한 JPA 리포지토리 인터페이스
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findAllByUser(User user); // 특정 User 가 제출한 모든 CodeSubmission 목록을 조회하는 메서드
    void deleteAllByUser(User user);
}
