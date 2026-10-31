package com.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String message;
    private String referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}