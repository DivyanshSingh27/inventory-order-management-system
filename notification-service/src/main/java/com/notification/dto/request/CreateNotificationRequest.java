package com.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNotificationRequest {

    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Message is required")
    private String message;

    private String referenceId;
    // Optional — links notification to an order or product
}