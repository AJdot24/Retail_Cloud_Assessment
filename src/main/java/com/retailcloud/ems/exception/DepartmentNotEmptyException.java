package com.retailcloud.ems.exception;

/**
 * Thrown when a department that still has employees assigned is deleted.
 * Mapped to HTTP 409 Conflict by the global exception handler.
 */
public class DepartmentNotEmptyException extends RuntimeException {

    public DepartmentNotEmptyException(String message) {
        super(message);
    }
}
