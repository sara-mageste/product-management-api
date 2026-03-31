package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor <Product>{
    Page<Product> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCode(String code, Pageable pageable);
    boolean existsByCode(String code);
}

