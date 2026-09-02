package com.adobe.printservice.service;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.adobe.printservice.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JobProcessorTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobProcessor jobProcessor;

    @Test
    void working_eventuallyCompletesOrFailsJob() {
        Job job = new Job();
        job.setTemplateId("b6f1e6a2-6b8b-4a9d-9c2e-3f2d8a2f9b10");
        jobRepository.save(job);

        for (int attempt = 0; attempt < 6; attempt++) {
            jobProcessor.working();
        }

        Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
        assertTrue(updatedJob.getStatus() == JobStatus.DONE || updatedJob.getStatus() == JobStatus.FAILED);
        assertTrue(updatedJob.getAttempts() >= 1 && updatedJob.getAttempts() <= 6);
    }
}
