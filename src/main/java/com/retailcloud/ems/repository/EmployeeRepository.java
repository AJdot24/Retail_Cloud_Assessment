package com.retailcloud.ems.repository;

import com.retailcloud.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Employee}.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Batch-fetches employees for a set of departments in a single query.
     * Used by the department list endpoint (with {@code expand=employee})
     * to avoid the classic N+1 problem.
     */
    List<Employee> findByDepartmentIdIn(Collection<Long> departmentIds);

    /** Returns true if at least one employee has no reporting manager. */
    boolean existsByReportingManagerIsNull();

    /** Counts employees assigned to the given department. */
    long countByDepartmentId(Long departmentId);

    /**
     * Fetches a page of employees together with their department and
     * reporting manager in one query (entity graph, avoids lazy-loading
     * N+1 when mapping to DTOs).
     */
    @Override
    @EntityGraph(attributePaths = {"department", "reportingManager"})
    Page<Employee> findAll(Pageable pageable);

    /**
     * Fetches a single employee with department and reporting manager
     * resolved in one query. LEFT JOIN FETCH is safe here because the
     * reporting manager is nullable.
     */
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.reportingManager WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);

    /**
     * Projection used by the {@code lookup=true} endpoint: only id and name
     * are selected, so the payload is lightweight.
     */
    @Query("SELECT e.id AS id, e.name AS name FROM Employee e")
    Page<EmployeeNameIdProjection> findIdsAndNames(Pageable pageable);

    /**
     * Projection interface for the lookup endpoint.
     */
    interface EmployeeNameIdProjection {
        Long getId();

        String getName();
    }
}
