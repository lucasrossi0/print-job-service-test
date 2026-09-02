package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;

import java.time.Instant;
import java.util.Map;

public record JobOutputApi(String id, String templateId, Map<String, Object> parameters, JobStatus status,
                           Instant createdAt, Instant updatedAt) {
    public static JobOutputApi fromJob(Job job) {
        return new JobOutputApi(
                job.getId(),
                job.getTemplateId(),
                job.getParameters(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
