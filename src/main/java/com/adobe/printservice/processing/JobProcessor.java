package com.adobe.printservice.processing;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.adobe.printservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobProcessor {

    private static final int MAX_ATTEMPTS = 3;

    private final JobRepository jobRepository;
    private final Rendering rendering;
    private final TransactionTemplate transactionTemplate;
    
    @Scheduled(fixedDelay = 1000)
    public void processJob() {
        claimNextJob().ifPresent(job -> {
            try {
                completeJob(job, rendering.renderDocument(job));
            } catch (RuntimeException exception) {
                handleFailure(job, exception);
            }
        });
    }

    private Optional<Job> claimNextJob() {
        return transactionTemplate.execute(ignored -> {
            Optional<Job> job = jobRepository.findNextQueuedForUpdate();
            job.ifPresent(this::updateToProcessing);
            return job;
        });
    }

    private void updateToProcessing(Job job) {
        job.setStatus(JobStatus.PROCESSING);
        job.setAttempts(job.getAttempts() + 1);
        saveJob(job);
    }

    private void completeJob(Job job, String result) {
        job.setResultContent(result);
        job.setStatus(JobStatus.DONE);
        job.setErrorMessage(null);
        saveJob(job);
    }

    private void handleFailure(Job job, RuntimeException exception) {
        if (job.getAttempts() < MAX_ATTEMPTS) {
            job.setStatus(JobStatus.QUEUED);
        } else {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(exception.getMessage());
        }
        saveJob(job);
    }

    private void saveJob(Job job) {
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
    }

}
