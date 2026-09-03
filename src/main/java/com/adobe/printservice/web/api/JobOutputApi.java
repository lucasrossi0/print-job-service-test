package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobOutputApi(String id, String templateId, Map<String, Object> parameters, JobStatus status,
                           Instant createdAt, Instant updatedAt) {
    public static JobOutputApi fromJob(Job job) {
        return new JobOutputApi(
                job.getId(),
                null,
                null,
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
