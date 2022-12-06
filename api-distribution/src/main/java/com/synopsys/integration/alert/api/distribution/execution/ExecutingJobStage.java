package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.UUID;

public class ExecutingJobStage {
    private final UUID jobId;
    private final JobStage stage;
    private final Instant start;
    private Instant end;

    public static ExecutingJobStage createStage(UUID jobId, String key, JobStage stage) {
        return new ExecutingJobStage(jobId, stage, Instant.now());
    }

    protected ExecutingJobStage(UUID jobId, JobStage stage, Instant start) {
        this.jobId = jobId;
        this.stage = stage;
        this.start = start;
    }

    public void endStage() {
        this.end = Instant.now();
    }

    public UUID getJobId() {
        return jobId;
    }

    public JobStage getStage() {
        return stage;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}
