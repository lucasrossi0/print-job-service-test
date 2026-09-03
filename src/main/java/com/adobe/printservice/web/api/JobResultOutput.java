package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResultOutput(JobStatus status, String errorContent, String resultContent, String situation) {
    private static final String QUEUED_SITUATION = "Job is going to be processed.";
    private static final String PROCESSING_SITUATION = "Job is being processed.";

    public static JobResultOutput fromJob(Job job) {
        return switch (job.getStatus()) {
            case DONE -> new JobResultOutput(JobStatus.DONE, null, job.getResultContent(), null);
            case FAILED -> new JobResultOutput(JobStatus.FAILED, job.getErrorMessage(), null, null);
            case QUEUED -> new JobResultOutput(job.getStatus(), null, null, QUEUED_SITUATION);
            case PROCESSING -> new JobResultOutput(job.getStatus(), null, null, PROCESSING_SITUATION);
        };
    }
}
