package com.saraprojects.product_api.service;

import com.saraprojects.product_api.enums.NotificationType;
import com.saraprojects.product_api.model.Notification;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createLowStockNotification(Product product) {

        Notification existingNotification =
                notificationRepository
                        .findFirstByProductIdAndResolvedFalse(product.getId())
                        .orElse(null);

        if (existingNotification != null) {

            existingNotification.setMessage(
                    product.getName() +
                            " has only " +
                            product.getQuantity() +
                            " units left in stock."
            );

            existingNotification.setRead(false);

            notificationRepository.save(existingNotification);

            return;
        }

        Notification notification = new Notification();

        notification.setTitle("Low stock");

        notification.setMessage(
                product.getName() +
                        " has only " +
                        product.getQuantity() +
                        " units left in stock."
        );

        notification.setType(NotificationType.LOW_STOCK);
        notification.setProductId(product.getId());
        notification.setResolved(false);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    public void resolveLowStockNotifications(Long productId) {

        List<Notification> notifications = notificationRepository.findByProductIdAndResolvedFalse(productId);

        notifications.forEach(notification -> {
            notification.setResolved(true);
        });

        notificationRepository.saveAll(notifications);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .filter(notification -> !notification.isResolved())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public Notification  markAsRead(Long id) {

        Notification notification = notificationRepository.findById(id).orElseThrow(() ->
            new RuntimeException("Notification not found")
        );

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public long countUnreadNotifications() {
        return notificationRepository.countByIsReadFalse();
    }
}
