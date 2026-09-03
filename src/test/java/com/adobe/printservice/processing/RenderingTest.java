package com.adobe.printservice.processing;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobMother;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderingTest {

    private static final String RENDERING_INTERRUPTED = "Rendering was interrupted";

    private final Rendering rendering = new Rendering();

    @Test
    void renderDocument_returnsTheSimulatedDocumentContent() {
        Job job = JobMother.invoiceJob();

        String result = rendering.renderDocument(job, false);

        assertEquals("""
                Rendered document
                Template: %s
                Parameters: %s""".formatted(JobMother.INVOICE_TEMPLATE_ID, JobMother.PARAMETERS), result);
    }

    @Test
    void renderDocument_throwsWhenRenderingFails() {
        Job job = new Job();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rendering.renderDocument(job, true)
        );

        assertEquals(JobMother.RENDERING_FAILURE, exception.getMessage());
    }

    @Test
    void renderDocument_throwsAndRestoresInterruptWhenThreadIsInterrupted() {
        Job job = JobMother.invoiceJob();
        Thread.currentThread().interrupt();

        try {
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> rendering.renderDocument(job, false)
            );

            assertEquals(RENDERING_INTERRUPTED, exception.getMessage());
            assertInstanceOf(InterruptedException.class, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
