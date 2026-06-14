package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByProductId(Long productId);
    List<Promotion> findByCategory(ProductCategory category);
}
