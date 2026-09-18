package com.saraprojects.product_api.service;

import com.saraprojects.product_api.dto.PromotionCreateDTO;
import com.saraprojects.product_api.dto.PromotionDTO;
import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.PromotionStatus;
import com.saraprojects.product_api.enums.PromotionTargetType;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.model.Promotion;
import com.saraprojects.product_api.repository.ProductRepository;
import com.saraprojects.product_api.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PromotionService promotionService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Sample Product")
                .price(BigDecimal.valueOf(100.00))
                .category(ProductCategory.FRUITS)
                .build();
    }

    // ==========================================
    // createPromotion() Tests
    // ==========================================

    @Test
    void createPromotion_startDateAfterEndDate_throwsRuntimeException() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(2); // End before start

        PromotionCreateDTO dto = new PromotionCreateDTO(
                PromotionTargetType.PRODUCT,
                1L,
                null,
                BigDecimal.valueOf(20),
                start,
                end
        );

        assertThatThrownBy(() -> promotionService.createPromotion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Start date cannot be after end date");

        verifyNoInteractions(repository, notificationService);
    }

    @Test
    void createPromotion_discountPercentageZeroOrNegative_throwsRuntimeException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        PromotionCreateDTO dtoZero = new PromotionCreateDTO(
                PromotionTargetType.PRODUCT,
                1L,
                null,
                BigDecimal.ZERO,
                start,
                end
        );

        assertThatThrownBy(() -> promotionService.createPromotion(dtoZero))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Discount percentage must be between 0 and 100");
    }

    @Test
    void createPromotion_discountPercentageGreaterThan100_throwsRuntimeException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        PromotionCreateDTO dtoOver100 = new PromotionCreateDTO(
                PromotionTargetType.PRODUCT,
                1L,
                null,
                BigDecimal.valueOf(105),
                start,
                end
        );

        assertThatThrownBy(() -> promotionService.createPromotion(dtoOver100))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Discount percentage must be between 0 and 100");
    }

    @Test
    void createPromotion_overlappingProductDateRange_throwsRuntimeException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        PromotionCreateDTO dto = new PromotionCreateDTO(
                PromotionTargetType.PRODUCT,
                1L,
                null,
                BigDecimal.valueOf(20),
                start,
                end
        );

        Promotion existingPromotion = Promotion.builder()
                .id(99L)
                .targetType(PromotionTargetType.PRODUCT)
                .product(sampleProduct)
                .startDate(start)
                .endDate(end)
                .discountPercentage(BigDecimal.valueOf(10))
                .status(PromotionStatus.SCHEDULED)
                .build();

        when(repository.findOverlappingByProduct(1L, start, end))
                .thenReturn(List.of(existingPromotion));

        assertThatThrownBy(() -> promotionService.createPromotion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This product already has a promotion overlapping this date range");

        verify(repository, never()).save(any());
    }

    @Test
    void createPromotion_overlappingCategoryDateRange_throwsRuntimeException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        PromotionCreateDTO dto = new PromotionCreateDTO(
                PromotionTargetType.CATEGORY,
                null,
                ProductCategory.FRUITS,
                BigDecimal.valueOf(15),
                start,
                end
        );

        Promotion existingCategoryPromotion = Promotion.builder()
                .id(88L)
                .targetType(PromotionTargetType.CATEGORY)
                .category(ProductCategory.FRUITS)
                .startDate(start)
                .endDate(end)
                .discountPercentage(BigDecimal.valueOf(10))
                .status(PromotionStatus.SCHEDULED)
                .build();

        when(repository.findOverlappingByCategory(ProductCategory.FRUITS, start, end))
                .thenReturn(List.of(existingCategoryPromotion));

        assertThatThrownBy(() -> promotionService.createPromotion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This category already has a promotion overlapping this date range");

        verify(repository, never()).save(any());
    }

    @Test
    void createPromotion_validProductPromotion_savesAndReturnsDTO() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        PromotionCreateDTO dto = new PromotionCreateDTO(
                PromotionTargetType.PRODUCT,
                1L,
                null,
                BigDecimal.valueOf(20),
                start,
                end
        );

        when(repository.findOverlappingByProduct(1L, start, end)).thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Promotion saved = Promotion.builder()
                .id(10L)
                .targetType(PromotionTargetType.PRODUCT)
                .product(sampleProduct)
                .discountPercentage(BigDecimal.valueOf(20))
                .startDate(start)
                .endDate(end)
                .status(PromotionStatus.SCHEDULED)
                .build();

        when(repository.save(any(Promotion.class))).thenReturn(saved);

        PromotionDTO result = promotionService.createPromotion(dto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.discountPercentage()).isEqualTo(BigDecimal.valueOf(20));

        verify(repository).save(any(Promotion.class));
        verify(notificationService).checkPromotionNotifications(saved);
    }

    // ==========================================
    // deletePromotion() and deletePromotions() Tests
    // ==========================================

    @Test
    void deletePromotion_activePromotion_throwsRuntimeException() {
        Promotion activePromotion = Promotion.builder()
                .id(1L)
                .status(PromotionStatus.ACTIVE)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(activePromotion));

        assertThatThrownBy(() -> promotionService.deletePromotion(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Active promotions cannot be deleted");

        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deletePromotion_scheduledPromotion_deletesSuccessfully() {
        Promotion scheduledPromotion = Promotion.builder()
                .id(2L)
                .status(PromotionStatus.SCHEDULED)
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(scheduledPromotion));

        promotionService.deletePromotion(2L);

        verify(repository).deleteById(2L);
    }

    @Test
    void deletePromotion_finishedPromotion_deletesSuccessfully() {
        Promotion finishedPromotion = Promotion.builder()
                .id(3L)
                .status(PromotionStatus.FINISHED)
                .build();

        when(repository.findById(3L)).thenReturn(Optional.of(finishedPromotion));

        promotionService.deletePromotion(3L);

        verify(repository).deleteById(3L);
    }

    @Test
    void deletePromotions_containsActivePromotion_throwsRuntimeException() {
        Promotion active = Promotion.builder().id(1L).status(PromotionStatus.ACTIVE).build();
        Promotion scheduled = Promotion.builder().id(2L).status(PromotionStatus.SCHEDULED).build();

        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(active, scheduled));

        assertThatThrownBy(() -> promotionService.deletePromotions(List.of(1L, 2L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Active promotions cannot be deleted");

        verify(repository, never()).deleteAllById(any());
    }

    @Test
    void deletePromotions_onlyScheduledAndFinished_deletesSuccessfully() {
        Promotion scheduled = Promotion.builder().id(1L).status(PromotionStatus.SCHEDULED).build();
        Promotion finished = Promotion.builder().id(2L).status(PromotionStatus.FINISHED).build();

        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(scheduled, finished));

        promotionService.deletePromotions(List.of(1L, 2L));

        verify(repository).deleteAllById(List.of(1L, 2L));
    }
}
