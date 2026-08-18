package com.retailcloud.ems.controller;

import com.retailcloud.ems.common.PageResponse;
import com.retailcloud.ems.service.AnalyticsService;
import com.retailcloud.ems.service.AnalyticsService.DepartmentAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Analytics endpoints computed from the employee and department data.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Per-department analytics: headcount, average salary, total salary and
     * total yearly bonus. Paginated like every other list endpoint
     * (20 rows per page by default).
     */
    @GetMapping("/departments")
    public PageResponse<DepartmentAnalytics> departmentAnalytics(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<DepartmentAnalytics> all = analyticsService.getDepartmentAnalytics();

        int from = Math.min((int) pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        Page<DepartmentAnalytics> result = new PageImpl<>(all.subList(from, to), pageable, all.size());
        return PageResponse.from(result);
    }
}
