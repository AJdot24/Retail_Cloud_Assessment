package com.retailcloud.ems.controller;

import com.retailcloud.ems.common.PageResponse;
import com.retailcloud.ems.dto.ChainNode;
import com.retailcloud.ems.dto.EmployeeLookup;
import com.retailcloud.ems.dto.EmployeeRequest;
import com.retailcloud.ems.dto.EmployeeResponse;
import com.retailcloud.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for employees.
 *
 * <p>All list endpoints are paginated by default (20 items per page).
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /** Creates a new employee. Returns 201 with the created employee. */
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    /** Fully updates an existing employee (PUT semantics: the whole record is replaced). */
    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    /**
     * Partially updates an employee by moving them to another department.
     * Only the department reference changes.
     */
    @PatchMapping("/{id}/department/{departmentId}")
    public EmployeeResponse moveToDepartment(@PathVariable Long id, @PathVariable Long departmentId) {
        return employeeService.moveToDepartment(id, departmentId);
    }

    /**
     * Lists employees.
     *
     * <p>Passing {@code lookup=true} returns a lightweight id/name list
     * instead of the full employee records. Both modes are paginated with
     * 20 items per page by default.
     */
    @GetMapping
    public PageResponse<?> listEmployees(@RequestParam(value = "lookup", defaultValue = "false") boolean lookup,
                                         @PageableDefault(size = 20) Pageable pageable) {
        if (lookup) {
            Page<EmployeeLookup> page = employeeService.lookup(pageable);
            return PageResponse.from(page);
        }
        return PageResponse.from(employeeService.list(pageable));
    }

    /** Fetches a single employee by id. */
    @GetMapping("/{id}")
    public EmployeeResponse getEmployee(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    /**
     * Returns the reporting chain of an employee: the employee themselves,
     * then their manager, up to the top-level employee.
     */
    @GetMapping("/{id}/reporting-chain")
    public List<ChainNode> getReportingChain(@PathVariable Long id) {
        return employeeService.getReportingChain(id);
    }
}
