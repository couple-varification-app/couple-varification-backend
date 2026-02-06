package com.relationshipplatform.exception;

/**
 * Exception thrown when user is below minimum age requirement (18+)
 */
public class AgeVerificationException extends RuntimeException {

    public AgeVerificationException(String message) {
        super(message);
    }

    public AgeVerificationException(int age) {
        super(String.format("User must be 18 or older. Current age: %d", age));
    }
}