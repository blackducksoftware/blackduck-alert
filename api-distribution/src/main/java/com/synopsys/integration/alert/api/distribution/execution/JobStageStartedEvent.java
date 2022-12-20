package com.synopsys.integration.alert.api.distribution.execution;

import java.util.UUID;

public class JobStageStartedEvent extends JobStageEvent {
    public static final String DEFAULT_DESTINATION_NAME = "job_stage_start_event";

    public JobStageStartedEvent(UUID jobExecutionId, JobStage jobStage) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobStage);
    }
}
