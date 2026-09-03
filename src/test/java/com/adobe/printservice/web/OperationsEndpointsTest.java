package com.adobe.printservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OperationsEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void liveness_isUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readiness_isUpWhenDatabaseIsAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void info_returnsApplicationMetadata() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("print-job-service"))
                .andExpect(jsonPath("$.app.version").value("0.0.1-SNAPSHOT"));
    }

    @Test
    void metrics_returnsAJobCountForEachStatus() throws Exception {
        mockMvc.perform(get("/jobs/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.QUEUED").isNumber())
                .andExpect(jsonPath("$.PROCESSING").isNumber())
                .andExpect(jsonPath("$.DONE").isNumber())
                .andExpect(jsonPath("$.FAILED").isNumber());
    }
}
