package com.codewise.repository;

import com.codewise.domain.AnalysisHistory;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findByUser(User user);

    boolean existsByUserAndIdempotencyKey(User user, String idempotencyKey);

    // User 엔티티 기반으로 모든 분석 히스토리 삭제 메서드 추가
    @Transactional
    void deleteAllByUser(User user);
}