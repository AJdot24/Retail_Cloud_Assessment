package com.retailcloud.ems.service;

import com.retailcloud.ems.dto.DepartmentRequest;
import com.retailcloud.ems.dto.DepartmentResponse;
import com.retailcloud.ems.dto.EmployeeResponse;
import com.retailcloud.ems.entity.Department;
import com.retailcloud.ems.entity.Employee;
import com.retailcloud.ems.exception.DepartmentNotEmptyException;
import com.retailcloud.ems.exception.InvalidOperationException;
import com.retailcloud.ems.exception.ResourceNotFoundException;
import com.retailcloud.ems.repository.DepartmentRepository;
import com.retailcloud.ems.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for departments: create, update, delete, list and fetch.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Department names must be unique.</li>
 *   <li>A department with employees assigned cannot be deleted (HTTP 409).</li>
 *   <li>The department head must be an existing employee.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private static final String DEPARTMENT = "Department";

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    /** Creates a department. {@code creationDate} defaults to today, head is optional. */
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new InvalidOperationException("Department with name '%s' already exists".formatted(request.name()));
        }

        Department department = new Department();
        department.setName(request.name());
        department.setCreationDate(request.creationDate() != null ? request.creationDate() : LocalDate.now());
        department.setDepartmentHead(resolveHead(request.departmentHeadId()));

        return map(departmentRepository.save(department), 0L);
    }

    /** Fully updates a department: name and head. Passing no head clears the current one. */
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT, id));

        if (departmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new InvalidOperationException("Department with name '%s' already exists".formatted(request.name()));
        }

        department.setName(request.name());
        if (request.creationDate() != null) {
            department.setCreationDate(request.creationDate());
        }
        department.setDepartmentHead(resolveHead(request.departmentHeadId()));

        return map(department, employeeRepository.countByDepartmentId(id));
    }

    /**
     * Deletes a department. Fails with HTTP 409 if employees are still
     * assigned to it — the caller must move them first.
     */
    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT, id));

        long assigned = employeeRepository.countByDepartmentId(id);
        if (assigned > 0) {
            throw new DepartmentNotEmptyException(
                    "Cannot delete department '%s' (id %d): %d employee(s) are still assigned; move them first"
                            .formatted(department.getName(), id, assigned));
        }

        departmentRepository.delete(department);
    }

    /**
     * Returns one page of departments. When {@code expand} is true, each
     * department additionally carries the full list of its employees.
     *
     * <p>The employee lists and the per-department counts are fetched with
     * two bulk queries (IN-clause + GROUP BY) instead of one query per
     * department, avoiding the N+1 problem.
     */
    public Page<DepartmentResponse> list(Pageable pageable, boolean expand) {
        Page<Department> page = departmentRepository.findAll(pageable);
        Map<Long, Long> counts = loadEmployeeCounts();
        if (!expand) {
            return page.map(d -> map(d, counts.getOrDefault(d.getId(), 0L)));
        }
        Map<Long, List<Employee>> employeesByDepartment =
                loadEmployeesByDepartment(page.getContent().stream().map(Department::getId).toList());
        return page.map(d -> {
            List<EmployeeResponse> employees = employeesByDepartment
                    .getOrDefault(d.getId(), List.of()).stream()
                    .map(EmployeeResponse::from)
                    .toList();
            return DepartmentResponse.from(d, counts.getOrDefault(d.getId(), 0L), employees);
        });
    }

    /** Returns a single department, optionally with its employees (expand=employee). */
    public DepartmentResponse getById(Long id, boolean expand) {
        Department department = expand
                ? departmentRepository.findWithEmployeesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT, id))
                : departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT, id));

        long employeeCount = expand ? department.getEmployees().size()
                : employeeRepository.countByDepartmentId(id);

        if (expand) {
            List<EmployeeResponse> employees = department.getEmployees().stream()
                    .map(EmployeeResponse::from)
                    .toList();
            return DepartmentResponse.from(department, employeeCount, employees);
        }
        return map(department, employeeCount);
    }

    private Map<Long, Long> loadEmployeeCounts() {
        return departmentRepository.countEmployeesByDepartment().stream()
                .collect(Collectors.toMap(
                        DepartmentRepository.EmployeeCountProjection::getId,
                        DepartmentRepository.EmployeeCountProjection::getEmployeeCount
                ));
    }

    private Map<Long, List<Employee>> loadEmployeesByDepartment(List<Long> departmentIds) {
        return employeeRepository.findByDepartmentIdIn(departmentIds).stream()
                .collect(Collectors.groupingBy(employee -> employee.getDepartment().getId()));
    }

    private DepartmentResponse map(Department department, long employeeCount) {
        return DepartmentResponse.from(department, employeeCount);
    }

    private Employee resolveHead(Long departmentHeadId) {
        if (departmentHeadId == null) {
            return null;
        }
        return employeeRepository.findById(departmentHeadId)
                .orElseThrow(() -> new ResourceNotFoundException("Department head", departmentHeadId));
    }
}
