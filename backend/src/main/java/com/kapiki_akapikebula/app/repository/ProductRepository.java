package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query(
        """ 
        SELECT DISTINCT p FROM Product p
        JOIN ShopProducts sp ON sp.product = p
        WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:minPrice IS NULL OR sp.price >= :minPrice)
        AND (:maxPrice IS NULL OR sp.price <= :maxPrice)
        """
    )
    Page<Product> search(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );
}
