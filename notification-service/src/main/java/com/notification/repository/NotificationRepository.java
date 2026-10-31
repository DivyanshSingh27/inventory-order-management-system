package com.notification.repository;

import com.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all unread notifications
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    // Get all notifications newest first
    List<Notification> findAllByOrderByCreatedAtDesc();

    // Get by type e.g. all ORDER_PLACED notifications
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);

    // Mark all unread as read in one query
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.isRead = false")
    int markAllAsRead();
}