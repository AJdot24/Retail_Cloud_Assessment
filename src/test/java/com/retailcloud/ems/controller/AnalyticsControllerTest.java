package com.retailcloud.ems.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the analytics endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void departmentAnalytics_returnsAggregatesForEveryDepartment() throws Exception {
        mockMvc.perform(get("/api/analytics/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Engineering"))
                .andExpect(jsonPath("$.content[0].headcount").value(9))
                .andExpect(jsonPath("$.content[0].averageSalary").isNumber())
                .andExpect(jsonPath("$.content[0].totalSalary").isNumber())
                .andExpect(jsonPath("$.content[0].totalYearlyBonus").isNumber());
    }
}
