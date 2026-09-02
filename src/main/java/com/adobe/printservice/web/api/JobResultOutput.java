package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResultOutput(JobStatus status, String errorContent, String resultContent) {
    public static JobResultOutput fromJob(Job job) {
        return switch (job.getStatus()) {
            case DONE -> new JobResultOutput(JobStatus.DONE, null, job.getResultContent());
            case FAILED -> new JobResultOutput(JobStatus.FAILED, job.getErrorMessage(), null);
            case QUEUED, PROCESSING -> new JobResultOutput(job.getStatus(), null, null);
        };
    }
}
