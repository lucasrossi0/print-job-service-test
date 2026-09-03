package com.adobe.printservice.service;

import com.adobe.printservice.model.Job;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class Rendering {

    private static final String RENDERING_FAILURE = "Simulated transient rendering failure";
    private static final String RENDERING_INTERRUPTED = "Rendering was interrupted";

    public String renderDocument(Job job) {
        return renderDocument(job, ThreadLocalRandom.current().nextInt(10) < 3);
    }

    String renderDocument(Job job, boolean shouldFail) {
        if (shouldFail) {
            throw new IllegalStateException(RENDERING_FAILURE);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(RENDERING_INTERRUPTED, exception);
        }

        return """
        Rendered document
        Template: %s
        Parameters: %s
        """.formatted(job.getTemplateId(), job.getParameters()).strip();
    }
}
