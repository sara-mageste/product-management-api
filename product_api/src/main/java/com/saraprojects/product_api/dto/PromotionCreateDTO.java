package com.saraprojects.product_api.dto;

import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.PromotionTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionCreateDTO(

        PromotionTargetType targetType,

        Long productId,

        ProductCategory category,

        BigDecimal discountPercentage,

        LocalDateTime startDate,

        LocalDateTime endDate

) {
}