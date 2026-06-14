package com.saraprojects.product_api.dto;

import com.saraprojects.product_api.enums.PromotionStatus;
import com.saraprojects.product_api.enums.PromotionTargetType;
import com.saraprojects.product_api.model.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionDTO(

        Long id,

        PromotionTargetType targetType,

        String targetName,

        BigDecimal discountPercentage,

        LocalDateTime startDate,

        LocalDateTime endDate,

        PromotionStatus status

) {

    public PromotionDTO(Promotion promotion) {

        this(
                promotion.getId(),
                promotion.getTargetType(),

                promotion.getTargetType() == PromotionTargetType.PRODUCT
                        && promotion.getProduct() != null
                        ? promotion.getProduct().getName()
                        : promotion.getCategory() != null
                        ? promotion.getCategory().name()
                        : null,

                promotion.getDiscountPercentage(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getStatus()
        );
    }
}