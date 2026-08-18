package com.retailcloud.ems.dto;

/**
 * Lightweight employee representation used by the {@code lookup=true} list
 * endpoint: only id and name are returned.
 */
public record EmployeeLookup(
        Long id,
        String name
) {
}
