package com.retailcloud.ems.exception;

/**
 * Thrown when a request violates a business rule (duplicate department name,
 * invalid reporting manager, circular reporting chain, second top-level
 * employee, ...). Mapped to HTTP 409 Conflict by the global exception handler.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
