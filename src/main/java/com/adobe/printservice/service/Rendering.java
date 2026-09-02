package com.adobe.printservice.service;

import com.adobe.printservice.model.Job;
import org.springframework.stereotype.Component;

@Component
public class Rendering {
    public String renderDocument(Job job) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Rendering was interrupted", exception);
        }

        return """
        Rendered document
        Template: %s
        Parameters: %s
        """.formatted(job.getTemplateId(), job.getParameters()).strip();
    }
}
