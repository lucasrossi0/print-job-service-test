package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;

import java.util.Map;

public record JobInputApi(String templateId, Map<String, Object> parameters) {
    public Job toJob() {
        Job job = new Job();
        job.setTemplateId(templateId);
        job.setParameters(parameters);
        return job;
    }
}
