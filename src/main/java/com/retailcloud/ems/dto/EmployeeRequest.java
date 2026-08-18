package com.retailcloud.ems.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating and fully updating an employee (POST/PUT).
 *
 * <p>All fields are required except {@code reportingManagerId}: the single
 * top-level employee of the organization has no reporting manager.
 */
public record EmployeeRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "dateOfBirth is required")
        @Past(message = "dateOfBirth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "salary is required")
        @DecimalMin(value = "0", inclusive = false, message = "salary must be greater than zero")
        BigDecimal salary,

        @NotNull(message = "departmentId is required")
        @Positive(message = "departmentId must be a positive number")
        Long departmentId,

        @NotBlank(message = "address is required")
        String address,

        @NotBlank(message = "role is required")
        String role,

        @NotNull(message = "joiningDate is required")
        @PastOrPresent(message = "joiningDate cannot be in the future")
        LocalDate joiningDate,

        @NotNull(message = "yearlyBonusPercentage is required")
        @DecimalMin(value = "0", message = "yearlyBonusPercentage must be between 0 and 100")
        @DecimalMax(value = "100", message = "yearlyBonusPercentage must be between 0 and 100")
        BigDecimal yearlyBonusPercentage,

        @Positive(message = "reportingManagerId must be a positive number")
        Long reportingManagerId
) {
}
