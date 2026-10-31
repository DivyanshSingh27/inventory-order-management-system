package com.notification.service.impl;

import com.notification.constants.AppConstants;
import com.notification.dto.request.CreateNotificationRequest;
import com.notification.dto.response.NotificationResponse;
import com.notification.entity.Notification;
import com.notification.exception.ResourceNotFoundException;
import com.notification.repository.NotificationRepository;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification of type: {}", request.getType());

        Notification notification = Notification.builder()
                .type(request.getType())
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        log.info("Fetching notification with id: {}", id);
        return mapToResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        log.info("Fetching unread notifications");
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByType(String type) {
        log.info("Fetching notifications of type: {}", type);
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        log.info("Marking notification {} as read", id);
        Notification notification = findById(id);
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        log.info("Marking all notifications as read");
        return notificationRepository.markAllAsRead();
    }

    // ─── Private helpers ────────────────────────────

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.NOTIFICATION_NOT_FOUND + id
                ));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}