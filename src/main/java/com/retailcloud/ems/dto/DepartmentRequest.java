package com.retailcloud.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Request body for creating and updating a department (POST/PUT).
 *
 * <p>{@code creationDate} defaults to today when omitted; {@code departmentHeadId}
 * is optional so that a department can be created without an employee existing yet
 * (avoids the cyclic foreign key problem: employee.department_id ↔ department.department_head_id).
 */
public record DepartmentRequest(
        @NotBlank(message = "name is required")
        String name,

        @PastOrPresent(message = "creationDate cannot be in the future")
        LocalDate creationDate,

        @Positive(message = "departmentHeadId must be a positive number")
        Long departmentHeadId
) {
}
