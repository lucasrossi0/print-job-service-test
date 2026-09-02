package com.adobe.printservice.service;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.adobe.printservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class JobProcessor {

    private final JobRepository jobRepository;
    private final Rendering rendering;
    private final TransactionTemplate transactionTemplate;
    
    @Scheduled(fixedDelay = 1000)
    public void working() {
        claimNextJob().ifPresent(job -> {
            try {
                completeJob(job, render(job));
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
        save(job);
    }

    private String render(Job job) {
        if (ThreadLocalRandom.current().nextInt(10) < 3) {
            throw new IllegalStateException("Simulated transient rendering failure");
        }
        return rendering.renderDocument(job);
    }

    private void completeJob(Job job, String result) {
        job.setResultContent(result);
        job.setStatus(JobStatus.DONE);
        job.setErrorMessage(null);
        save(job);
    }

    private void handleFailure(Job job, RuntimeException exception) {
        if (job.getAttempts() < maxAttempts(job)) {
            job.setStatus(JobStatus.QUEUED);
        } else {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(exception.getMessage());
        }
        save(job);
    }

    private void save(Job job) {
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);
    }

    private int maxAttempts(Job job) {
        return 3 + Math.floorMod(job.getId().hashCode(), 4);
    }
}
