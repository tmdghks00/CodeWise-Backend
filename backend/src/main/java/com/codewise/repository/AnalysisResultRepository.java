package com.codewise.repository;

import com.codewise.domain.AnalysisResult;
import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByCodeSubmission(CodeSubmission submission);

    List<AnalysisResult> findAllByCodeSubmission_User_Email(String email);

    List<AnalysisResult> findAllByCodeSubmission_User(User user);

    List<AnalysisResult> findByCodeSubmission_User_Id(Long userId);

    @Modifying
    @Query("DELETE FROM AnalysisResult ar WHERE ar.codeSubmission.user = :user")
    void deleteAllByCodeSubmission_User(@Param("user") User user);


    // submissionId + userId 조합 조회
    Optional<AnalysisResult> findByCodeSubmission_IdAndCodeSubmission_User_Id(Long submissionId, Long userId);
}
