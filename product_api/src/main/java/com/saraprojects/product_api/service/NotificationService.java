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

        notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }
}
