package com.saraprojects.product_api.model;

import jakarta.persistence.*;
import lombok.*;
import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column
    private ProductStatus status;

    @Lob
    @Column(name = "image_url", columnDefinition = "MEDIUMTEXT")
    private String imageUrl;

    @Column(unique = true, nullable = false, length = 20)
    private String code;
}

