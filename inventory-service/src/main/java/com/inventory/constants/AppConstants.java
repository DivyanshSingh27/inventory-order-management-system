package com.inventory.constants;


public final class AppConstants {


    private AppConstants() {}

    // ─── Success Messages ───────────────────────────
    public static final String PRODUCT_CREATED      = "Product created successfully";
    public static final String PRODUCT_UPDATED      = "Product updated successfully";
    public static final String PRODUCT_DELETED      = "Product deleted successfully";
    public static final String STOCK_UPDATED        = "Stock updated successfully";

    // ─── Error Messages ─────────────────────────────
    public static final String PRODUCT_NOT_FOUND    = "Product not found with id: ";
    public static final String DUPLICATE_SKU        = "Product with this SKU already exists: ";
    public static final String INSUFFICIENT_STOCK   = "Insufficient stock for product id: ";
    public static final String INVALID_QUANTITY     = "Quantity must be greater than zero";

    // ─── Field Names (used in validation messages) ──
    public static final String FIELD_NAME           = "name";
    public static final String FIELD_SKU            = "sku";
    public static final String FIELD_QUANTITY       = "quantity";
    public static final String FIELD_PRICE          = "price";
}