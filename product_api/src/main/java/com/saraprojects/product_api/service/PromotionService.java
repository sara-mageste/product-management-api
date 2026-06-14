package com.saraprojects.product_api.service;

import com.saraprojects.product_api.dto.PromotionCreateDTO;
import com.saraprojects.product_api.dto.PromotionDTO;
import com.saraprojects.product_api.enums.PromotionStatus;
import com.saraprojects.product_api.enums.PromotionTargetType;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.model.Promotion;
import com.saraprojects.product_api.repository.ProductRepository;
import com.saraprojects.product_api.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository repository;
    private final ProductRepository productRepository;

    // Return all promotions
    public List<PromotionDTO> getAllPromotions() {
        return repository.findAll()
                .stream()
                .map(PromotionDTO::new)
                .toList();
    }

    // Find promotion by ID
    public PromotionDTO getPromotionById(Long id) {

        Promotion promotion = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Promotion not found with ID: " + id));

        return new PromotionDTO(promotion);
    }

    // Create new promotion
    public PromotionDTO createPromotion(PromotionCreateDTO dto) {

        if (dto.startDate().isAfter(dto.endDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        if (dto.discountPercentage().compareTo(BigDecimal.ZERO) <= 0
                || dto.discountPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new RuntimeException(
                    "Discount percentage must be between 0 and 100");
        }

        Promotion promotion = new Promotion();

        promotion.setTargetType(dto.targetType());
        promotion.setDiscountPercentage(dto.discountPercentage());
        promotion.setStartDate(dto.startDate());
        promotion.setEndDate(dto.endDate());

        if (dto.targetType() == PromotionTargetType.PRODUCT) {

            if (dto.productId() == null) {
                throw new RuntimeException("Product is required");
            }

            Product product = productRepository.findById(dto.productId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found"));

            promotion.setProduct(product);
        }

        if (dto.targetType() == PromotionTargetType.CATEGORY) {

            if (dto.category() == null) {
                throw new RuntimeException("Category is required");
            }

            promotion.setCategory(dto.category());
        }

        Promotion saved = repository.save(promotion);

        return new PromotionDTO(saved);
    }

    // Delete promotion
    public void deletePromotion(Long id) {

        Promotion promotion = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Promotion not found with ID: " + id));

        if (promotion.getStatus() != PromotionStatus.FINISHED) {
            throw new RuntimeException(
                    "Only finished promotions can be deleted");
        }

        repository.deleteById(id);
    }

    // Delete selected promotions
    public void deletePromotions(List<Long> ids) {

        List<Promotion> promotions = repository.findAllById(ids);

        boolean hasInvalidPromotion = promotions.stream()
                .anyMatch(p ->
                        p.getStatus() != PromotionStatus.FINISHED);

        if (hasInvalidPromotion) {
            throw new RuntimeException(
                    "Only finished promotions can be deleted");
        }

        repository.deleteAllById(ids);
    }

    // Get active promotion for product (product promotion has priority over category promotion)
    public Promotion getActivePromotionForProduct(Product product) {

        List<Promotion> productPromotions =
                repository.findByProductId(product.getId());

        Promotion activeProductPromotion = productPromotions.stream()
                .filter(p -> p.getStatus() == PromotionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (activeProductPromotion != null) {
            return activeProductPromotion;
        }

        List<Promotion> categoryPromotions =
                repository.findByCategory(product.getCategory());

        return categoryPromotions.stream()
                .filter(p -> p.getStatus() == PromotionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
}
