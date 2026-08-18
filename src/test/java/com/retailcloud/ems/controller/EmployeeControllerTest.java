package com.retailcloud.ems.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the employee endpoints against the seeded
 * in-memory database (3 departments, 25 employees).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String NEW_EMPLOYEE = """
            {
              "name": "Test Employee",
              "dateOfBirth": "1995-05-05",
              "salary": 750000.00,
              "departmentId": 1,
              "address": "Test City",
              "role": "Software Engineer",
              "joiningDate": "2024-01-01",
              "yearlyBonusPercentage": 10.0,
              "reportingManagerId": 2
            }
            """;

    @Test
    void createEmployee_returns201WithCreatedResource() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.reportingManagerName").value("Priya Nair"));
    }

    @Test
    void createEmployee_secondTopLevelEmployee_returns409() throws Exception {
        String request = NEW_EMPLOYEE.replace("\"reportingManagerId\": 2", "\"reportingManagerId\": null");
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("top-level employee already exists")));
    }

    @Test
    void createEmployee_unknownManager_returns404() throws Exception {
        String request = NEW_EMPLOYEE.replace("\"reportingManagerId\": 2", "\"reportingManagerId\": 999");
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEmployee_missingRequiredFields_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "salary": 750000.00,
                                  "departmentId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
    }

    @Test
    void updateEmployee_selfAsManager_returns409() throws Exception {
        String request = NEW_EMPLOYEE.replace("\"reportingManagerId\": 2", "\"reportingManagerId\": 5");
        mockMvc.perform(put("/api/employees/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void moveEmployeeToAnotherDepartment_returnsUpdatedEmployee() throws Exception {
        mockMvc.perform(patch("/api/employees/{employeeId}/department/{departmentId}", 5, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.departmentId").value(2))
                .andExpect(jsonPath("$.departmentName").value("Sales"))
                .andExpect(jsonPath("$.name").value("Vikram Reddy"));
    }

    @Test
    void moveEmployeeToUnknownDepartment_returns404() throws Exception {
        mockMvc.perform(patch("/api/employees/{employeeId}/department/{departmentId}", 5, 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEmployees_isPaginatedByDefaultWith20Items() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void listEmployees_withLookup_returnsOnlyIdAndName() throws Exception {
        mockMvc.perform(get("/api/employees").param("lookup", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[0].name").isString())
                .andExpect(jsonPath("$.content[0].salary").doesNotExist());
    }

    @Test
    void getEmployeeById_returnsFullDetails() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vikram Reddy"))
                .andExpect(jsonPath("$.role").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.reportingManagerName").value("Priya Nair"))
                .andExpect(jsonPath("$.yearlyBonusPercentage").value(12.0));
    }

    @Test
    void getEmployeeById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void reportingChain_walksUpToTopLevelEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/reporting-chain", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Vikram Reddy"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Priya Nair"))
                .andExpect(jsonPath("$[2].id").value(1))
                .andExpect(jsonPath("$[2].role").value("Chief Executive Officer"));
    }

    @Test
    void reportingChain_unknownEmployee_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/reporting-chain", 999))
                .andExpect(status().isNotFound());
    }
}
