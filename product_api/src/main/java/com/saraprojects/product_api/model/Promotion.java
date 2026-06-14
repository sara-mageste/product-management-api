package com.saraprojects.product_api.model;

import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.PromotionStatus;
import com.saraprojects.product_api.enums.PromotionTargetType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionTargetType targetType;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(nullable = false)
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    public PromotionStatus getStatus() {
        return calculateStatus();
    }

    public PromotionStatus calculateStatus() {

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startDate)) {
            return PromotionStatus.SCHEDULED;
        }

        if (now.isAfter(endDate)) {
            return PromotionStatus.FINISHED;
        }

        return PromotionStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
