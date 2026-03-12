package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;
import com.saraprojects.product_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Searches for products whose name **starts** with the given text, ignoring uppercase/lowercase
    Page<Product> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategoryAndStatus(
        ProductCategory category,
        ProductStatus status,
        Pageable pageable
    );
}

