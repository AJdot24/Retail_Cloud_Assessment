package com.retailcloud.ems.service;

import com.retailcloud.ems.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Analytics over the employee/department data, computed with a single
 * aggregated JPQL query (GROUP BY) — no client-side iteration of all rows.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DepartmentRepository departmentRepository;

    public AnalyticsService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Per-department analytics: headcount, average salary, total salary and
     * total yearly bonus (salary * bonus% / 100 summed over all employees).
     */
    public List<DepartmentAnalytics> getDepartmentAnalytics() {
        return departmentRepository.findDepartmentAnalytics();
    }

    /**
     * Projection of a single department's analytics row.
     */
    public interface DepartmentAnalytics {
        Long getId();

        String getName();

        Long getHeadcount();

        BigDecimal getAverageSalary();

        BigDecimal getTotalSalary();

        BigDecimal getTotalYearlyBonus();
    }
}
