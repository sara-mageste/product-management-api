package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;
import com.saraprojects.product_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCode(String code, Pageable pageable);
    boolean existsByCode(String code);
    Page<Product> findByCategoryIn(List<ProductCategory> categories, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryInAndStatus(
        List<ProductCategory> categories,
        ProductStatus status,
        Pageable pageable
    );
}

