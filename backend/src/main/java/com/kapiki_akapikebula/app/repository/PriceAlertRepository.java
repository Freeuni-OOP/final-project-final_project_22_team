package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
}