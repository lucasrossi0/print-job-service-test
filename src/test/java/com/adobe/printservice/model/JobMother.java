package com.adobe.printservice.model;

public final class JobMother {

    public static final String INVOICE_TEMPLATE_ID = "b6f1e6a2-6b8b-4a9d-9c2e-3f2d8a2f9b10";
    public static final String RENDERED_INVOICE = "Rendered invoice";
    public static final String RENDERER_UNAVAILABLE = "Renderer unavailable";

    public static Job failedJob() {
        Job job = jobWithStatus(JobStatus.FAILED);
        job.setErrorMessage(RENDERER_UNAVAILABLE);
        return job;
    }

    public static Job doneJob() {
        Job job = jobWithStatus(JobStatus.DONE);
        job.setResultContent(RENDERED_INVOICE);
        return job;
    }

    public static Job processingJob() {
        return jobWithStatus(JobStatus.PROCESSING);
    }

    private static Job jobWithStatus(JobStatus status) {
        Job job = new Job();
        job.setTemplateId(INVOICE_TEMPLATE_ID);
        job.setStatus(status);
        return job;
    }
}
