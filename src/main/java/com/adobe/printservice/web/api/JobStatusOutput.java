package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobStatusOutput(
        JobStatus status,
        int attempts,
        boolean resultAvailable,
        String errorMessage
) {
    public static JobStatusOutput fromJob(Job job) {
        return new JobStatusOutput(
                job.getStatus(),
                job.getAttempts(),
                job.getResultContent() != null,
                job.getStatus() == JobStatus.FAILED ? job.getErrorMessage() : null
        );
    }
}
