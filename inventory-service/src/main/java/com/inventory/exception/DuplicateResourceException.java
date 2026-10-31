package com.inventory.exception;

/**
 * Thrown when trying to create a resource that already exists.
 * Example: creating a product with a SKU that's already taken.
 * Maps to HTTP 409 Conflict in the GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}