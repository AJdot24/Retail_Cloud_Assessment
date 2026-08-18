package com.retailcloud.ems.exception;

/**
 * Thrown when a resource (employee, department) is not found.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("%s with id %s not found".formatted(resourceName, identifier));
    }
}
