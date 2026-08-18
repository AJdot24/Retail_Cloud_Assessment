package com.retailcloud.ems.dto;

/**
 * One node of a reporting chain: the employee plus the manager (and so on)
 * up to the top-level employee.
 */
public record ChainNode(
        Long id,
        String name,
        String role
) {
}
