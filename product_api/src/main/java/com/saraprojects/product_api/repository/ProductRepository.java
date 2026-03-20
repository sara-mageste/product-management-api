package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;
import com.saraprojects.product_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCode(String code, Pageable pageable);
    boolean existsByCode(String code);
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(
        ProductCategory category,
        ProductStatus status,
        Pageable pageable
    );
}

