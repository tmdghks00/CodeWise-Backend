package com.codewise.repository;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// AnalysisResult 데이터를 DB 에서 조회/저장하기 위한 JPA 리포지토리 인터페이스
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    // 특정 CodeSubmission 에 대한 분석 결과를 조회하는 메서드
    Optional<AnalysisResult> findByCodeSubmission(CodeSubmission submission);

    // 특정 사용자 이메일로 제출된 코드들의 모든 분석 결과를 조회하는 메서드
    List<AnalysisResult> findAllByCodeSubmission_User_Email(String email);

    // 특정 User 객체로 제출된 코드들의 모든 분석 결과를 조회하는 메서드
    List<AnalysisResult> findAllByCodeSubmission_User(User user);

    // 특정 사용자 ID로 제출된 코드들의 모든 분석 결과를 조회하는 메서드
    List<AnalysisResult> findByCodeSubmission_User_Id(Long userId);
}
