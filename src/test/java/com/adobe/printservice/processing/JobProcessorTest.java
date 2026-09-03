package com.adobe.printservice.processing;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobMother;
import com.adobe.printservice.model.JobStatus;
import com.adobe.printservice.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

@SpringBootTest
class JobProcessorTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobProcessor jobProcessor;

    @MockitoBean
    private Rendering rendering;

    @Test
    void processJob_marksSuccessfulRenderAsDone() {
        Job job = JobMother.queuedJob();
        when(rendering.renderDocument(argThat(
                actualJob -> actualJob.getId().equals(job.getId())
        ))).thenReturn(JobMother.RENDERED_INVOICE);

        jobRepository.save(job);

        jobProcessor.processJob();

        Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.DONE, updatedJob.getStatus());
        assertEquals(1, updatedJob.getAttempts());
        assertEquals(JobMother.RENDERED_INVOICE, updatedJob.getResultContent());
        verify(rendering, times(1)).renderDocument(argThat(
                actualJob -> actualJob.getId().equals(job.getId())
        ));
    }

    @Test
    void processJob_reQueuesJobAfterNonFinalFailure() {
        Job job = JobMother.queuedJob();
        when(rendering.renderDocument(argThat(
                actualJob -> actualJob.getId().equals(job.getId())
        ))).thenThrow(new IllegalStateException(JobMother.RENDERING_FAILURE));

        jobRepository.save(job);

        jobProcessor.processJob();

        Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.QUEUED, updatedJob.getStatus());
        assertEquals(1, updatedJob.getAttempts());
        assertNull(updatedJob.getErrorMessage());
        verify(rendering).renderDocument(argThat(
                actual -> actual.getId().equals(job.getId())
        ));
    }

    @Test
    void processJob_marksJobAsFailedAfterFinalFailure() {
        Job job = JobMother.queuedJob();
        job.setAttempts(2);
        when(rendering.renderDocument(argThat(
                actualJob -> actualJob.getId().equals(job.getId())
        ))).thenThrow(new IllegalStateException(JobMother.RENDERING_FAILURE));
        jobRepository.save(job);

        jobProcessor.processJob();

        Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
        assertEquals(JobStatus.FAILED, updatedJob.getStatus());
        assertEquals(3, updatedJob.getAttempts());
        assertEquals(JobMother.RENDERING_FAILURE, updatedJob.getErrorMessage());
        verify(rendering, times(1)).renderDocument(argThat(
                actualJob -> actualJob.getId().equals(job.getId())
        ));
    }

}
