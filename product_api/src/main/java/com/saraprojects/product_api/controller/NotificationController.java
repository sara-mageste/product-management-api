package com.saraprojects.product_api.controller;

import com.saraprojects.product_api.model.Notification;
import com.saraprojects.product_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getNotifications() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PutMapping("/mark-all-read")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    @GetMapping("/unread/count")
    public long countUnreadNotifications() {
        return notificationService.countUnreadNotifications();
    }
}
