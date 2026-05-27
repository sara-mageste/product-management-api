package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByProductIdAndResolvedFalse(Long productId);
    List<Notification> findByResolvedFalse();
    List<Notification> findByProductIdAndResolvedFalse(Long productId);
    Optional<Notification> findFirstByProductIdAndResolvedFalse(Long productId);
    long countByIsReadFalse();
}
