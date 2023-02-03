package com.synopsys.integration.alert.api.distribution.execution;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ExecutingJobStage {
    private final UUID executionId;
    private final JobStage stage;
    private final Instant start;
    private Instant end;

    public static ExecutingJobStage createStage(UUID executionId, JobStage stage, Instant start) {
        return new ExecutingJobStage(executionId, stage, start);
    }

    protected ExecutingJobStage(UUID executionId, JobStage stage, Instant start) {
        this.executionId = executionId;
        this.stage = stage;
        this.start = start;
    }

    public void endStage(Instant end) {
        this.end = end;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public JobStage getStage() {
        return stage;
    }

    public Instant getStart() {
        return start;
    }

    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }
}
