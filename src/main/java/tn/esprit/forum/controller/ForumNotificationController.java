package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.entity.ForumNotification;
import tn.esprit.forum.services.ForumNotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forums")
public class ForumNotificationController {

    @Autowired
    private ForumNotificationService notificationService;

    @PostMapping("/create-notification")
    public ResponseEntity<ForumNotification> createNotification(@RequestBody ForumNotification notification) {
        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    @GetMapping("/get-notifications/{userId}")
    public ResponseEntity<List<ForumNotification>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @GetMapping("/get-unread-notifications/{userId}")
    public ResponseEntity<List<ForumNotification>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadByUser(userId));
    }

    @GetMapping("/get-unread-count/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/mark-notification-read/{id}")
    public ResponseEntity<ForumNotification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/mark-all-notifications-read/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-notification/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
