package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.ShopProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopProductsRepository extends JpaRepository<ShopProducts, Long> {
    List<ShopProducts> findByProductIdOrderByPriceAsc(long productId);
    List<ShopProducts> findByProductId(Long productId);
}
