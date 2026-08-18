package com.retailcloud.ems.dto;

import com.retailcloud.ems.entity.Department;

import java.time.LocalDate;
import java.util.List;

/**
 * Department representation returned by the API.
 *
 * <p>{@code employees} is {@code null} unless the request used
 * {@code expand=employee}, keeping the default response lightweight.
 */
public record DepartmentResponse(
        Long id,
        String name,
        LocalDate creationDate,
        Long departmentHeadId,
        String departmentHeadName,
        long employeeCount,
        List<EmployeeResponse> employees
) {

    /** Maps a department without expanding its employees. */
    public static DepartmentResponse from(Department department, long employeeCount) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCreationDate(),
                department.getDepartmentHead() != null ? department.getDepartmentHead().getId() : null,
                department.getDepartmentHead() != null ? department.getDepartmentHead().getName() : null,
                employeeCount,
                null
        );
    }

    /** Maps a department together with its employees (expand=employee). */
    public static DepartmentResponse from(Department department, long employeeCount, List<EmployeeResponse> employees) {
        DepartmentResponse base = from(department, employeeCount);
        return new DepartmentResponse(
                base.id(),
                base.name(),
                base.creationDate(),
                base.departmentHeadId(),
                base.departmentHeadName(),
                base.employeeCount(),
                employees
        );
    }
}
