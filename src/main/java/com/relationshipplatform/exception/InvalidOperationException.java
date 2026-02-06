package com.relationshipplatform.exception;

/**
 * Exception thrown when an operation is not allowed
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}