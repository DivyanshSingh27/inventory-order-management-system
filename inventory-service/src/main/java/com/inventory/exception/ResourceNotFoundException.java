package com.inventory.exception;

/**
 * Thrown when a requested resource does not exist in the DB.
 * Maps to HTTP 404 in the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}