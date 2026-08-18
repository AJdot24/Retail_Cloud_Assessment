package com.retailcloud.ems.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the department endpoints against the seeded
 * in-memory database (3 departments, 25 employees).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createDepartment_returns201() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Finance",
                                  "departmentHeadId": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Finance"))
                .andExpect(jsonPath("$.creationDate").isString())
                .andExpect(jsonPath("$.departmentHeadName").value("Vikram Reddy"))
                .andExpect(jsonPath("$.employeeCount").value(0));
    }

    @Test
    void createDepartment_duplicateName_returns409() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Engineering"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void createDepartment_unknownHead_returns404() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Finance", "departmentHeadId": 999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDepartment_withAssignedEmployees_returns409() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("9 employee(s) are still assigned")));
    }

    @Test
    void deleteDepartment_emptyDepartment_returns204() throws Exception {
        String createdBody = mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Temporary"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(createdBody).get("id").asLong();

        mockMvc.perform(delete("/api/departments/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDepartment_unknownId_returns404() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDepartments_returnsPaginatedDepartmentsWithCounts() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Engineering"))
                .andExpect(jsonPath("$.content[0].employeeCount").value(9))
                .andExpect(jsonPath("$.content[0].departmentHeadName").value("Priya Nair"))
                .andExpect(jsonPath("$.content[0].employees").doesNotExist());
    }

    @Test
    void listDepartments_withExpandEmployee_includesEmployees() throws Exception {
        mockMvc.perform(get("/api/departments").param("expand", "employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Engineering"))
                .andExpect(jsonPath("$.content[0].employees", hasSize(9)))
                .andExpect(jsonPath("$.content[0].employees[0].id").isNumber())
                .andExpect(jsonPath("$.content[1].employees", hasSize(8)))
                .andExpect(jsonPath("$.content[2].employees", hasSize(8)));
    }

    @Test
    void getDepartment_withExpandEmployee_returnsEmployees() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", 2).param("expand", "employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sales"))
                .andExpect(jsonPath("$.employees", hasSize(8)))
                .andExpect(jsonPath("$.departmentHeadName").value("Rohan Mehta"));
    }
}
