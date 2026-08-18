package com.retailcloud.ems.service;

import com.retailcloud.ems.dto.ChainNode;
import com.retailcloud.ems.dto.EmployeeLookup;
import com.retailcloud.ems.dto.EmployeeRequest;
import com.retailcloud.ems.dto.EmployeeResponse;
import com.retailcloud.ems.entity.Department;
import com.retailcloud.ems.entity.Employee;
import com.retailcloud.ems.exception.InvalidOperationException;
import com.retailcloud.ems.exception.ResourceNotFoundException;
import com.retailcloud.ems.repository.DepartmentRepository;
import com.retailcloud.ems.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for employees: create, update, move between departments,
 * list (paginated / lookup mode), fetch by id and reporting chains.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Every employee except the single top-level employee must have a
 *       reporting manager (exactly one top-level employee is allowed).</li>
 *   <li>A manager cannot be set to the employee itself, and a change must
 *       not introduce a circular reporting chain.</li>
 *   <li>An employee can only belong to a department that exists.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private static final String EMPLOYEE = "Employee";

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new employee after validating department, reporting manager
     * and the single-top-level-employee rule.
     */
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Department department = findDepartment(request.departmentId());
        Employee manager = resolveManager(request.reportingManagerId());

        if (manager == null && employeeRepository.existsByReportingManagerIsNull()) {
            throw new InvalidOperationException(
                    "A top-level employee already exists; every new employee must have a reporting manager");
        }

        Employee employee = new Employee();
        apply(employee, request, department, manager);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    /**
     * Fully updates an existing employee (PUT semantics: the request replaces
     * every field, including department and reporting manager).
     */
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE, id));

        Department department = findDepartment(request.departmentId());
        Employee manager = resolveManager(request.reportingManagerId());

        if (manager != null && manager.getId().equals(id)) {
            throw new InvalidOperationException("An employee cannot be their own reporting manager");
        }
        if (manager == null && employee.getReportingManager() != null) {
            throw new InvalidOperationException(
                    "This employee already has a reporting manager; remove the existing manager first "
                            + "(a top-level employee already exists)");
        }
        if (manager != null) {
            validateNoCycle(id, manager);
        }

        apply(employee, request, department, manager);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    /**
     * Moves an employee to another department (PATCH, partial update).
     * Only the department reference changes; all other fields stay untouched.
     */
    @Transactional
    public EmployeeResponse moveToDepartment(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE, employeeId));
        Department department = findDepartment(departmentId);

        employee.setDepartment(department);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    /** Returns one page of employees with department and manager resolved. */
    public Page<EmployeeResponse> list(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(EmployeeResponse::from);
    }

    /** Returns one page of id/name pairs (lookup=true). */
    public Page<EmployeeLookup> lookup(Pageable pageable) {
        return employeeRepository.findIdsAndNames(pageable)
                .map(projection -> new EmployeeLookup(projection.getId(), projection.getName()));
    }

    /** Returns a single employee by id. */
    public EmployeeResponse getById(Long id) {
        return employeeRepository.findByIdWithDetails(id)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE, id));
    }

    /**
     * Walks the reporting chain from the given employee upwards to the
     * top-level employee and returns the ordered chain.
     *
     * <p>The walk is protected against circular references: if the same
     * employee appears twice, the data is corrupt and a conflict is raised.
     */
    public List<ChainNode> getReportingChain(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE, employeeId));

        List<ChainNode> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Employee current = employee;

        while (current != null) {
            if (!visited.add(current.getId())) {
                throw new InvalidOperationException(
                        "Circular reporting chain detected at employee id " + current.getId() + "; data is inconsistent");
            }
            chain.add(new ChainNode(current.getId(), current.getName(), current.getRole()));
            current = current.getReportingManager();
        }
        return chain;
    }

    private Department findDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
    }

    private Employee resolveManager(Long reportingManagerId) {
        if (reportingManagerId == null) {
            return null;
        }
        return employeeRepository.findById(reportingManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporting manager", reportingManagerId));
    }

    /**
     * Walks from the proposed manager upwards; if the chain reaches the
     * employee being updated, setting this manager would create a cycle.
     */
    private void validateNoCycle(Long employeeId, Employee manager) {
        Set<Long> visited = new HashSet<>();
        Employee current = manager;
        while (current != null) {
            if (current.getId().equals(employeeId)) {
                throw new InvalidOperationException(
                        "Setting employee " + employeeId + " under this manager would create a circular reporting chain");
            }
            if (!visited.add(current.getId())) {
                throw new InvalidOperationException("Circular reporting chain detected in existing data");
            }
            current = current.getReportingManager();
        }
    }

    private void apply(Employee employee, EmployeeRequest request, Department department, Employee manager) {
        employee.setName(request.name());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setSalary(request.salary());
        employee.setDepartment(department);
        employee.setAddress(request.address());
        employee.setRole(request.role());
        employee.setJoiningDate(request.joiningDate());
        employee.setYearlyBonusPercentage(request.yearlyBonusPercentage());
        employee.setReportingManager(manager);
    }
}
