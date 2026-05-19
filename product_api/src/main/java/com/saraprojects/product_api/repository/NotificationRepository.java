package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
