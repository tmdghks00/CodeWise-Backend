package com.codewise.repository;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByCodeSubmission(CodeSubmission submission);

    List<AnalysisResult> findAllByCodeSubmission_User_Email(String email);

    List<AnalysisResult> findAllByCodeSubmission_User(User user);

    List<AnalysisResult> findByCodeSubmission_User_Id(Long userId);

    // submissionId + userId 조합 조회
    Optional<AnalysisResult> findByCodeSubmission_IdAndCodeSubmission_User_Id(Long submissionId, Long userId);
}
