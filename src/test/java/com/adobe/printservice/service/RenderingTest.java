package com.adobe.printservice.service;

import com.adobe.printservice.model.Job;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderingTest {

    @Test
    void renderDocument_returnsTheSimulatedDocumentContent() {
        Job job = new Job();
        job.setTemplateId("invoice-standard");
        job.setParameters(Map.of("customer", "Ada"));

        String result = new Rendering().renderDocument(job);

        assertEquals("""
                Rendered document
                Template: invoice-standard
                Parameters: {customer=Ada}""", result);
    }
}
