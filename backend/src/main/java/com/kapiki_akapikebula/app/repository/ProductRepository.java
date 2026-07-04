package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(
            value = """
            SELECT DISTINCT p FROM Product p
            JOIN FETCH p.shopProducts sp
            JOIN FETCH sp.shop
            WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                                   OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:minPrice IS NULL OR sp.price >= :minPrice)
              AND (:maxPrice IS NULL OR sp.price <= :maxPrice)
        """,
            countQuery = """
            SELECT COUNT(DISTINCT p) FROM Product p
            JOIN ShopProducts sp ON sp.product = p
            WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                                   OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:minPrice IS NULL OR sp.price >= :minPrice)
              AND (:maxPrice IS NULL OR sp.price <= :maxPrice)
        """
    )
    Page<Product> search(
            @Param("query") String query,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}