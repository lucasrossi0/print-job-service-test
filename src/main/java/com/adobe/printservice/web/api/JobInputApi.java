package com.adobe.printservice.web.api;

import com.adobe.printservice.model.Job;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class JobInputApi {
    private String templateId;
    private Map<String, Object> parameters;

    public Job toJob() {
        Job job = new Job();
        job.setTemplateId(templateId);
        job.setParameters(parameters);
        return job;
    }
}
