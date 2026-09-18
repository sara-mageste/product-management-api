package com.saraprojects.product_api.service;

import com.saraprojects.product_api.enums.NotificationType;
import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.PromotionTargetType;
import com.saraprojects.product_api.model.Notification;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.model.Promotion;
import com.saraprojects.product_api.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Low Stock Item")
                .price(BigDecimal.valueOf(50.00))
                .quantity(3)
                .category(ProductCategory.FRUITS)
                .build();
    }

    @Test
    void createLowStockNotification_calledTwiceForSameUnresolvedProduct_updatesExistingNotification() {
        // First call: no existing unresolved notification
        when(notificationRepository.findFirstByProductIdAndResolvedFalse(1L))
                .thenReturn(Optional.empty());

        notificationService.createLowStockNotification(sampleProduct);

        ArgumentCaptor<Notification> captor1 = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor1.capture());
        Notification firstCreated = captor1.getValue();
        assertThat(firstCreated.getTitle()).isEqualTo("Low stock");
        assertThat(firstCreated.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(firstCreated.getMessage()).contains("3 units left in stock.");

        // Simulate existing unresolved notification in repository
        firstCreated.setId(100L);
        when(notificationRepository.findFirstByProductIdAndResolvedFalse(1L))
                .thenReturn(Optional.of(firstCreated));

        // Update product quantity for second call
        sampleProduct.setQuantity(2);
        notificationService.createLowStockNotification(sampleProduct);

        ArgumentCaptor<Notification> captor2 = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor2.capture());

        Notification secondSaved = captor2.getAllValues().get(1);
        assertThat(secondSaved.getId()).isEqualTo(100L); // Same existing notification updated
        assertThat(secondSaved.getMessage()).contains("2 units left in stock.");
    }

    @Test
    void checkPromotionNotifications_promotionStartAndEndTomorrow_createsOnlySingleDayNotification() {
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime tomorrowEnd = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0);

        Promotion singleDayPromotion = Promotion.builder()
                .id(50L)
                .targetType(PromotionTargetType.PRODUCT)
                .product(sampleProduct)
                .startDate(tomorrowStart)
                .endDate(tomorrowEnd)
                .discountPercentage(BigDecimal.valueOf(15))
                .build();

        when(notificationRepository.findFirstByPromotionIdAndType(50L, NotificationType.PROMOTION_SINGLE_DAY))
                .thenReturn(Optional.empty());

        notificationService.checkPromotionNotifications(singleDayPromotion);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification created = captor.getValue();
        assertThat(created.getType()).isEqualTo(NotificationType.PROMOTION_SINGLE_DAY);
        assertThat(created.getPromotionId()).isEqualTo(50L);
        assertThat(created.getTitle()).isEqualTo("Promotion happening tomorrow");
    }

    @Test
    void checkPromotionNotifications_calledTwiceForSamePromotion_doesNotDuplicateNotification() {
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime tomorrowEnd = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0);

        Promotion singleDayPromotion = Promotion.builder()
                .id(50L)
                .targetType(PromotionTargetType.PRODUCT)
                .product(sampleProduct)
                .startDate(tomorrowStart)
                .endDate(tomorrowEnd)
                .discountPercentage(BigDecimal.valueOf(15))
                .build();

        Notification existingNotification = new Notification();
        existingNotification.setId(200L);
        existingNotification.setType(NotificationType.PROMOTION_SINGLE_DAY);

        when(notificationRepository.findFirstByPromotionIdAndType(50L, NotificationType.PROMOTION_SINGLE_DAY))
                .thenReturn(Optional.of(existingNotification));

        notificationService.checkPromotionNotifications(singleDayPromotion);

        verify(notificationRepository, never()).save(any());
    }
}
