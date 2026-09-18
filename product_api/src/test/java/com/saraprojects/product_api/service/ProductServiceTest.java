package com.saraprojects.product_api.service;

import com.saraprojects.product_api.dto.ProductDTO;
import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.ProductStatus;
import com.saraprojects.product_api.enums.PromotionStatus;
import com.saraprojects.product_api.enums.PromotionTargetType;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.model.Promotion;
import com.saraprojects.product_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Description")
                .price(new BigDecimal("8.40"))
                .quantity(50)
                .category(ProductCategory.FRUITS)
                .status(ProductStatus.ACTIVE)
                .imageUrl("http://example.com/img.png")
                .code("PROD-001")
                .build();
    }

    @Test
    void buildProductDTO_productWithActivePromotion_calculatesDiscountedPriceWithBigDecimal() {
        // Price: 8.40, Discount: 5% -> Expected price: 8.40 * 0.95 = 7.98 (tests precision where double historically fails)
        Promotion activePromotion = Promotion.builder()
                .id(10L)
                .targetType(PromotionTargetType.PRODUCT)
                .product(sampleProduct)
                .discountPercentage(new BigDecimal("5"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(3))
                .status(PromotionStatus.ACTIVE)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(promotionService.getActivePromotionForProduct(sampleProduct)).thenReturn(activePromotion);

        ProductDTO dto = productService.getProductById(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getOriginalPrice()).isEqualTo(new BigDecimal("8.40"));
        assertThat(dto.getPrice()).isEqualTo(new BigDecimal("7.98"));
        assertThat(dto.getDiscountPercentage()).isEqualTo(new BigDecimal("5"));
    }

    @Test
    void buildProductDTO_productWithoutPromotion_returnsOriginalPriceEqualPrice() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(promotionService.getActivePromotionForProduct(sampleProduct)).thenReturn(null);

        ProductDTO dto = productService.getProductById(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getOriginalPrice()).isEqualTo(new BigDecimal("8.40"));
        assertThat(dto.getPrice()).isEqualTo(new BigDecimal("8.40").setScale(2, RoundingMode.HALF_UP));
        assertThat(dto.getDiscountPercentage()).isNull();
    }
}
