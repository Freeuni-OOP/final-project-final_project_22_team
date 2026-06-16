package com.kapiki_akapikebula.app.repository;

import com.kapiki_akapikebula.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
