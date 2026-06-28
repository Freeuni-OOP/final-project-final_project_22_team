package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUserId(Long userID);

    Optional<PriceAlert> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT pa FROM PriceAlert pa WHERE pa.productId = :productId AND pa.targetPrice >= :currentPrice")
    List<PriceAlert> findTriggeredAlerts(@Param("productId") Long productId, @Param("currentPrice") BigDecimal currentPrice);
}