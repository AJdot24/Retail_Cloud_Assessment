package com.retailcloud.ems.dto;

import com.retailcloud.ems.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee representation returned by the API. Includes the department
 * and reporting manager as flattened references to avoid nested entities.
 */
public record EmployeeResponse(
        Long id,
        String name,
        LocalDate dateOfBirth,
        BigDecimal salary,
        Long departmentId,
        String departmentName,
        String address,
        String role,
        LocalDate joiningDate,
        BigDecimal yearlyBonusPercentage,
        Long reportingManagerId,
        String reportingManagerName
) {

    /** Maps an entity to a response DTO. Safe to call inside a transaction (lazy associations). */
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getDateOfBirth(),
                employee.getSalary(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getAddress(),
                employee.getRole(),
                employee.getJoiningDate(),
                employee.getYearlyBonusPercentage(),
                employee.getReportingManager() != null ? employee.getReportingManager().getId() : null,
                employee.getReportingManager() != null ? employee.getReportingManager().getName() : null
        );
    }
}
