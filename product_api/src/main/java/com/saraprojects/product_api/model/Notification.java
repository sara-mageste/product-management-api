package com.saraprojects.product_api.model;

import com.saraprojects.product_api.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean isRead = false;

    private boolean resolved = false;

    private LocalDateTime createdAt;

    private Long productId;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
