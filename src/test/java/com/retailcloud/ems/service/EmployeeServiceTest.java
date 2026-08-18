package com.retailcloud.ems.service;

import com.retailcloud.ems.dto.ChainNode;
import com.retailcloud.ems.dto.EmployeeRequest;
import com.retailcloud.ems.entity.Department;
import com.retailcloud.ems.entity.Employee;
import com.retailcloud.ems.exception.InvalidOperationException;
import com.retailcloud.ems.exception.ResourceNotFoundException;
import com.retailcloud.ems.repository.DepartmentRepository;
import com.retailcloud.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the business rules of {@link EmployeeService}:
 * reporting chains, cycle detection and manager validation.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private EmployeeService employeeService;

    private Employee employee(long id, String name, Employee manager) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setRole("Engineer");
        e.setReportingManager(manager);
        return e;
    }

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, departmentRepository);
    }

    @Test
    void getReportingChain_walksManagersUpToTopLevel() {
        Employee ceo = employee(1, "CEO", null);
        Employee manager = employee(2, "Manager", ceo);
        Employee engineer = employee(5, "Engineer", manager);

        // The chain walk follows the in-memory object graph (getReportingManager),
        // so only the starting employee needs to be loaded from the repository.
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(engineer));

        List<ChainNode> chain = employeeService.getReportingChain(5L);

        assertThat(chain).extracting(ChainNode::id).containsExactly(5L, 2L, 1L);
        assertThat(chain).extracting(ChainNode::name).containsExactly("Engineer", "Manager", "CEO");
    }

    @Test
    void getReportingChain_detectsCircularReference() {
        Employee a = employee(5, "A", null);
        Employee b = employee(6, "B", a);
        a.setReportingManager(b);

        when(employeeRepository.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> employeeService.getReportingChain(5L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Circular reporting chain");
    }

    @Test
    void getReportingChain_unknownEmployee_throwsNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getReportingChain(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEmployee_rejectsSelfAsManager() {
        Employee employee = employee(5, "Vikram", null);
        when(employeeRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));

        EmployeeRequest request = new EmployeeRequest(
                "Vikram Reddy",
                LocalDate.of(1992, 3, 11),
                new BigDecimal("1800000.00"),
                1L,
                "Bengaluru",
                "Senior Software Engineer",
                LocalDate.of(2021, 1, 11),
                new BigDecimal("12.00"),
                5L
        );

        assertThatThrownBy(() -> employeeService.update(5L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("their own reporting manager");
    }

    @Test
    void updateEmployee_managerChainLeadingBackToEmployee_isRejected() {
        Employee employee = employee(5, "Vikram", null);
        Employee manager = employee(2, "Priya", employee); // Priya reports to Vikram -> cycle

        when(employeeRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L)));

        EmployeeRequest request = new EmployeeRequest(
                "Vikram Reddy",
                LocalDate.of(1992, 3, 11),
                new BigDecimal("1800000.00"),
                1L,
                "Bengaluru",
                "Senior Software Engineer",
                LocalDate.of(2021, 1, 11),
                new BigDecimal("12.00"),
                2L
        );

        assertThatThrownBy(() -> employeeService.update(5L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("circular reporting chain");
    }

    private Department department(long id) {
        Department department = new Department();
        department.setId(id);
        department.setName("Engineering");
        return department;
    }
}
