package com.notification.controller;

import com.notification.constants.AppConstants;
import com.notification.dto.request.CreateNotificationRequest;
import com.notification.dto.response.ApiResponse;
import com.notification.dto.response.NotificationResponse;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        log.info("POST /api/v1/notifications - type: {}", request.getType());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.NOTIFICATION_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        log.info("GET /api/v1/notifications");
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched successfully",
                        notificationService.getAllNotifications())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(
            @PathVariable Long id) {
        log.info("GET /api/v1/notifications/{}", id);
        return ResponseEntity.ok(
                ApiResponse.success("Notification fetched successfully",
                        notificationService.getNotificationById(id))
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread() {
        log.info("GET /api/v1/notifications/unread");
        return ResponseEntity.ok(
                ApiResponse.success("Unread notifications fetched",
                        notificationService.getUnreadNotifications())
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByType(
            @PathVariable String type) {
        log.info("GET /api/v1/notifications/type/{}", type);
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched by type",
                        notificationService.getNotificationsByType(type))
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id) {
        log.info("PATCH /api/v1/notifications/{}/read", id);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.NOTIFICATION_READ,
                        notificationService.markAsRead(id))
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        log.info("PATCH /api/v1/notifications/read-all");
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.ALL_READ, count)
        );
    }
}