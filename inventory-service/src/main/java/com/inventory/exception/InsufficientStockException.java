package com.inventory.exception;

/**
 * Thrown when Order Service tries to deduct more stock
 * than is available for a product.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}