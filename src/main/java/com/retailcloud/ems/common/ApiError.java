package com.retailcloud.ems.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform error response body produced by the global exception handler.
 *
 * @param timestamp   when the error occurred
 * @param status      HTTP status code
 * @param error       HTTP status reason phrase
 * @param message     human-readable error description
 * @param path        request path that failed
 * @param fieldErrors field-level validation errors (only for 400 validation failures)
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
