package com.codewise.repository;

import com.codewise.domain.AnalysisHistory;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // [추가]
import org.springframework.data.jpa.repository.Query; // [추가]
import org.springframework.data.repository.query.Param; // [추가]
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findByUser(User user);

    boolean existsByUserAndIdempotencyKey(User user, String idempotencyKey);

    // [추가] 특정 User가 남긴 모든 AnalysisHistory 기록을 삭제하는 JPQL 쿼리
    @Modifying // 데이터 변경 쿼리임을 명시
    @Query("DELETE FROM AnalysisHistory ah WHERE ah.user = :user")
    void deleteAllByUser(@Param("user") User user);
}