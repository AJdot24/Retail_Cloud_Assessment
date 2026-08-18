package com.retailcloud.ems.repository;

import com.retailcloud.ems.entity.Department;
import com.retailcloud.ems.service.AnalyticsService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Department}.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Returns true if a department with the given name already exists. */
    boolean existsByName(String name);

    /** Returns true if a department with the given name exists, excluding a specific id (used for updates). */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Fetches a department together with all its employees in one query.
     * Used by the single-department endpoint when {@code expand=employee}.
     */
    @EntityGraph(attributePaths = "employees")
    Optional<Department> findWithEmployeesById(Long id);

    /**
     * One aggregated query returning the employee count per department.
     * Used to populate {@code employeeCount} on the paginated department
     * list without issuing one count query per department (N+1 again).
     */
    @Query("""
            SELECT d.id AS id, COUNT(e.id) AS employeeCount
            FROM Department d LEFT JOIN d.employees e
            GROUP BY d.id
            """)
    List<EmployeeCountProjection> countEmployeesByDepartment();

    /**
     * Single aggregated query computing per-department analytics:
     * headcount, average salary, total salary and total yearly bonus.
     */
    @Query("""
            SELECT d.id                    AS id,
                   d.name                  AS name,
                   COUNT(e.id)             AS headcount,
                   COALESCE(AVG(e.salary), 0) AS averageSalary,
                   COALESCE(SUM(e.salary), 0) AS totalSalary,
                   COALESCE(SUM(e.salary * e.yearlyBonusPercentage / 100), 0) AS totalYearlyBonus
            FROM Department d LEFT JOIN d.employees e
            GROUP BY d.id, d.name
            ORDER BY d.id
            """)
    List<AnalyticsService.DepartmentAnalytics> findDepartmentAnalytics();

    /** Projection of department id and its employee count. */
    interface EmployeeCountProjection {
        Long getId();

        Long getEmployeeCount();
    }
}
