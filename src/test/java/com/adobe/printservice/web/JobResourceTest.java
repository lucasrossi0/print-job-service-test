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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobResourceTest {

    private static final String JOBS_ENDPOINT = "/jobs";
    private static final String JOB_STATUS_ENDPOINT = "/jobs/{id}";
    private static final String JOB_RESULT_ENDPOINT = "/jobs/{id}/result";
    private static final String UNKNOWN_JOB_ID = "does-not-exist";
    private static final String JOB_NOT_FOUND = "Job not found";
    private static final String FAILED = "FAILED";
    private static final String DONE = "DONE";
    private static final String QUEUED = "QUEUED";
    private static final String STATUS_PATH = "$.status";
    private static final String ATTEMPTS_PATH = "$.attempts";
    private static final String RESULT_AVAILABLE_PATH = "$.resultAvailable";
    private static final String ERROR_MESSAGE_PATH = "$.errorMessage";
    private static final String DETAIL_PATH = "$.detail";
    private static final String RESULT_CONTENT_PATH = "$.resultContent";
    private static final String ERROR_CONTENT_PATH = "$.errorContent";
    private static final String SITUATION_PATH = "$.situation";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void createJob_withUnknownTemplate_returns400() throws Exception {
        mockMvc.perform(post(JOBS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"does-not-exist\",\"parameters\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_usesOnlyRequestFields() throws Exception {
        mockMvc.perform(post(JOBS_ENDPOINT)
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
                .andExpect(jsonPath(STATUS_PATH).value(QUEUED))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getAllJobs_withStatus_returnsOnlyMatchingJobs() throws Exception {
        Job failedJob = JobMother.failedJob();
        jobRepository.save(failedJob);

        mockMvc.perform(get(JOBS_ENDPOINT).param("status", FAILED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + failedJob.getId() + "')].status").value(FAILED))
                .andExpect(jsonPath("$[0].attempts").doesNotExist());
    }

    @Test
    void getJobStatus_whenJobFailed_returnsAttemptsAndError() throws Exception {
        Job failedJob = JobMother.failedJob();
        failedJob.setAttempts(3);
        jobRepository.save(failedJob);

        mockMvc.perform(get(JOB_STATUS_ENDPOINT, failedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value(FAILED))
                .andExpect(jsonPath(ATTEMPTS_PATH).value(3))
                .andExpect(jsonPath(RESULT_AVAILABLE_PATH).value(true))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(JobMother.RENDERING_FAILURE));
    }

    @Test
    void getJobStatus_whenJobIsDone_reportsAvailableResult() throws Exception {
        Job doneJob = JobMother.doneJob();
        jobRepository.save(doneJob);

        mockMvc.perform(get(JOB_STATUS_ENDPOINT, doneJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value(DONE))
                .andExpect(jsonPath(ATTEMPTS_PATH).value(0))
                .andExpect(jsonPath(RESULT_AVAILABLE_PATH).value(true))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).doesNotExist());
    }

    @Test
    void getJobStatus_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get(JOB_STATUS_ENDPOINT, UNKNOWN_JOB_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(DETAIL_PATH).value(JOB_NOT_FOUND));
    }

    @Test
    void getResult_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get(JOB_RESULT_ENDPOINT, UNKNOWN_JOB_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(DETAIL_PATH).value(JOB_NOT_FOUND));
    }

    @Test
    void getResult_whenJobIsDone_returnsResultContent() throws Exception {
        Job doneJob = JobMother.doneJob();
        jobRepository.save(doneJob);

        mockMvc.perform(get(JOB_RESULT_ENDPOINT, doneJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value(DONE))
                .andExpect(jsonPath(RESULT_CONTENT_PATH).value(JobMother.RENDERED_INVOICE))
                .andExpect(jsonPath(ERROR_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(SITUATION_PATH).doesNotExist());
    }

    @Test
    void getResult_whenJobFailed_returnsErrorContent() throws Exception {
        Job failedJob = JobMother.failedJob();
        jobRepository.save(failedJob);

        mockMvc.perform(get(JOB_RESULT_ENDPOINT, failedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value(FAILED))
                .andExpect(jsonPath(ERROR_CONTENT_PATH).value(JobMother.RENDERING_FAILURE))
                .andExpect(jsonPath(RESULT_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(SITUATION_PATH).doesNotExist());
    }

    @Test
    void getResult_whenJobIsProcessing_returnsProcessingSituation() throws Exception {
        Job processingJob = JobMother.processingJob();
        jobRepository.save(processingJob);

        mockMvc.perform(get(JOB_RESULT_ENDPOINT, processingJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("PROCESSING"))
                .andExpect(jsonPath(ERROR_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(RESULT_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(SITUATION_PATH).value("Job is being processed."));
    }

    @Test
    @Transactional
    void getResult_whenJobIsQueued_returnsQueuedSituation() throws Exception {
        Job queuedJob = JobMother.queuedJob();
        jobRepository.save(queuedJob);

        mockMvc.perform(get(JOB_RESULT_ENDPOINT, queuedJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value(QUEUED))
                .andExpect(jsonPath(ERROR_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(RESULT_CONTENT_PATH).doesNotExist())
                .andExpect(jsonPath(SITUATION_PATH).value("Job is going to be processed."));
    }
}
