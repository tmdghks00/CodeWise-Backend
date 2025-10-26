package com.codewise.repository;

import com.codewise.domain.AnalysisHistory;
import com.codewise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findByUser(User user);

    boolean existsByUserAndIdempotencyKey(User user, String idempotencyKey);

}