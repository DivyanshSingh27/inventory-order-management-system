package com.notification.service;

import com.notification.dto.request.CreateNotificationRequest;
import com.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(Long id);

    List<NotificationResponse> getAllNotifications();

    List<NotificationResponse> getUnreadNotifications();

    List<NotificationResponse> getNotificationsByType(String type);

    NotificationResponse markAsRead(Long id);

    int markAllAsRead();
}