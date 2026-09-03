package com.adobe.printservice.web;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobMother;
import com.adobe.printservice.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void createJob_withUnknownTemplate_returns400() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"does-not-exist\",\"parameters\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_usesOnlyRequestFields() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": "%s",
                                  "parameters": {"customer": "Lucas"},
                                  "status": "DONE",
                                  "attempts": 99
                                }
                                """.formatted(JobMother.INVOICE_TEMPLATE_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.templateId").value(JobMother.INVOICE_TEMPLATE_ID))
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getAllJobs_withStatus_returnsOnlyMatchingJobs() throws Exception {
        Job failedJob = JobMother.failedJob();
        jobRepository.save(failedJob);

        mockMvc.perform(get("/jobs").param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + failedJob.getId() + "')].status").value("FAILED"))
                .andExpect(jsonPath("$[0].attempts").doesNotExist());
    }

    @Test
    void getResult_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/jobs/{id}/result", "does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Job not found"));
    }

    @Test
    void getResult_whenJobIsDone_returnsResultContent() throws Exception {
        Job doneJob = JobMother.doneJob();
        jobRepository.save(doneJob);

        mockMvc.perform(get("/jobs/{id}/result", doneJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.resultContent").value(JobMother.RENDERED_INVOICE))
                .andExpect(jsonPath("$.errorContent").doesNotExist());
    }

    @Test
    void getResult_whenJobFailed_returnsErrorContent() throws Exception {
        Job failedJob = JobMother.failedJob();
        jobRepository.save(failedJob);

        mockMvc.perform(get("/jobs/{id}/result", failedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorContent").value(JobMother.RENDERING_FAILURE))
                .andExpect(jsonPath("$.resultContent").doesNotExist());
    }

    @Test
    void getResult_whenJobIsProcessing_returnsOnlyStatus() throws Exception {
        Job processingJob = JobMother.processingJob();
        jobRepository.save(processingJob);

        mockMvc.perform(get("/jobs/{id}/result", processingJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.errorContent").doesNotExist())
                .andExpect(jsonPath("$.resultContent").doesNotExist());
    }
}
