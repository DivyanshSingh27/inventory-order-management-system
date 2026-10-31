package com.notification.constants;

public final class AppConstants {

    private AppConstants() {}

    // ─── Success Messages ───────────────────────────
    public static final String NOTIFICATION_CREATED  = "Notification created successfully";
    public static final String NOTIFICATION_READ     = "Notification marked as read";
    public static final String ALL_READ              = "All notifications marked as read";

    // ─── Error Messages ─────────────────────────────
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found with id: ";

    // ─── Notification Types ─────────────────────────
    public static final String TYPE_ORDER_PLACED     = "ORDER_PLACED";
    public static final String TYPE_LOW_STOCK        = "LOW_STOCK";
    public static final String TYPE_ORDER_CANCELLED  = "ORDER_CANCELLED";
    public static final String TYPE_GENERAL          = "GENERAL";
}