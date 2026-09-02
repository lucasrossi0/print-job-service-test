package com.adobe.printservice.web;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
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

    private static final String INVOICE_TEMPLATE_ID = "b6f1e6a2-6b8b-4a9d-9c2e-3f2d8a2f9b10";

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
                                """.formatted(INVOICE_TEMPLATE_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.templateId").value(INVOICE_TEMPLATE_ID))
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getAllJobs_withStatus_returnsOnlyMatchingJobs() throws Exception {
        Job failedJob = new Job();
        failedJob.setTemplateId(INVOICE_TEMPLATE_ID);
        failedJob.setStatus(JobStatus.FAILED);
        jobRepository.save(failedJob);

        mockMvc.perform(get("/jobs").param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + failedJob.getId() + "')].status").value("FAILED"))
                .andExpect(jsonPath("$[0].attempts").doesNotExist());
    }

    @Test
    void getResult_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/jobs/{id}/result", "does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getResult_whenJobIsDone_returnsResultContent() throws Exception {
        Job doneJob = new Job();
        doneJob.setTemplateId(INVOICE_TEMPLATE_ID);
        doneJob.setStatus(JobStatus.DONE);
        doneJob.setResultContent("Rendered invoice");
        jobRepository.save(doneJob);

        mockMvc.perform(get("/jobs/{id}/result", doneJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.resultContent").value("Rendered invoice"))
                .andExpect(jsonPath("$.errorContent").doesNotExist());
    }

    @Test
    void getResult_whenJobFailed_returnsErrorContent() throws Exception {
        Job failedJob = new Job();
        failedJob.setTemplateId(INVOICE_TEMPLATE_ID);
        failedJob.setStatus(JobStatus.FAILED);
        failedJob.setErrorMessage("Renderer unavailable");
        jobRepository.save(failedJob);

        mockMvc.perform(get("/jobs/{id}/result", failedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorContent").value("Renderer unavailable"))
                .andExpect(jsonPath("$.resultContent").doesNotExist());
    }

    @Test
    void getResult_whenJobIsProcessing_returnsOnlyStatus() throws Exception {
        Job processingJob = new Job();
        processingJob.setTemplateId(INVOICE_TEMPLATE_ID);
        processingJob.setStatus(JobStatus.PROCESSING);
        jobRepository.save(processingJob);

        mockMvc.perform(get("/jobs/{id}/result", processingJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.errorContent").doesNotExist())
                .andExpect(jsonPath("$.resultContent").doesNotExist());
    }
}
