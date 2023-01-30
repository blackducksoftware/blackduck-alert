package com.synopsys.integration.alert.api.distribution.execution;

import java.util.UUID;

public class JobStageStartedEvent extends JobStageEvent {
    public static final String DEFAULT_DESTINATION_NAME = "job_stage_start_event";

    private final long startTimeMilliseconds;

    public JobStageStartedEvent(UUID jobExecutionId, JobStage jobStage, long startTimeMilliseconds) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobStage);
        this.startTimeMilliseconds = startTimeMilliseconds;
    }

    public long getStartTimeMilliseconds() {
        return startTimeMilliseconds;
    }
}
