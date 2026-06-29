package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductIdOrderByRecordedAtAsc(Long productId);

    @Query("SELECT p FROM PriceHistory p WHERE p.product.id = :productId AND p.recordedAt > :startDate ORDER BY p.recordedAt ASC")
    List<PriceHistory> getPriceHistory(Long productId, LocalDateTime startDate);
}
