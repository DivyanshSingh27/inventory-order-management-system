package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Universal API response wrapper.
 *
 * Every endpoint returns this shape:
 * {
 *   "success": true,
 *   "message": "Product created successfully",
 *   "data": { ...product... },
 *   "timestamp": "2025-01-01T10:00:00"
 * }
 *
 * Why wrap responses?
 *   Clients always know what to expect.
 *   Success/error state is explicit — no guessing
 *   from HTTP status code alone.
 *
 * Generic type T lets us reuse this for any data type:
 *   ApiResponse<ProductResponse>
 *   ApiResponse<List<ProductResponse>>
 *   ApiResponse<Void>  ← for deletes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ─── Static factory methods ──────────────────────

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}