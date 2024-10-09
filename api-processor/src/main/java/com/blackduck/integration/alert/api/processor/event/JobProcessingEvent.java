package com.blackduck.integration.alert.api.processor.event;

import java.util.UUID;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class JobProcessingEvent extends AlertEvent {
    private static final long serialVersionUID = 2069118377239056038L;
    public static final String JOB_PROCESSING_EVENT_TYPE = "event_processing_job";

    private UUID correlationId;
    private UUID jobId;

    public JobProcessingEvent(UUID correlationId, UUID jobId) {
        super(JOB_PROCESSING_EVENT_TYPE);
        this.correlationId = correlationId;
        this.jobId = jobId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getJobId() {
        return jobId;
    }
}
