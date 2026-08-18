package com.retailcloud.ems.controller;

import com.retailcloud.ems.common.PageResponse;
import com.retailcloud.ems.dto.DepartmentRequest;
import com.retailcloud.ems.dto.DepartmentResponse;
import com.retailcloud.ems.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for departments.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /** Creates a new department. Returns 201 with the created department. */
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    /** Fully updates a department (name and department head). */
    @PutMapping("/{id}")
    public DepartmentResponse updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    /**
     * Deletes a department. Fails with 409 Conflict if employees are still
     * assigned to the department — they must be moved or deleted first.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists departments (paginated, 20 per page by default).
     *
     * <p>Passing {@code expand=employee} adds the full list of employees
     * assigned to each department to the response.
     */
    @GetMapping
    public PageResponse<DepartmentResponse> listDepartments(
            @RequestParam(value = "expand", defaultValue = "") String expand,
            @PageableDefault(size = 20) Pageable pageable) {
        boolean expandEmployees = "employee".equals(expand);
        Page<DepartmentResponse> page = departmentService.list(pageable, expandEmployees);
        return PageResponse.from(page);
    }

    /** Fetches a single department, optionally expanded with its employees. */
    @GetMapping("/{id}")
    public DepartmentResponse getDepartment(
            @PathVariable Long id,
            @RequestParam(value = "expand", defaultValue = "") String expand) {
        return departmentService.getById(id, "employee".equals(expand));
    }
}
