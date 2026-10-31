package com.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;
    // e.g. ORDER_PLACED, LOW_STOCK, ORDER_CANCELLED

    @Column(nullable = false)
    private String message;

    @Column
    private String referenceId;
    // e.g. orderId or productId that triggered this notification

    @Column(nullable = false)
    private Boolean isRead = false;
    // false = unread, true = read

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;
    // timestamp when notification was marked as read

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}