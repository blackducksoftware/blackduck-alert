package com.synopsys.integration.alert.api.distribution.execution;

import java.util.UUID;

public class JobStageEndedEvent extends JobStageEvent {
    private static final long serialVersionUID = -5480882643467010869L;
    public static final String DEFAULT_DESTINATION_NAME = "job_stage_end_event";

    public JobStageEndedEvent(UUID jobExecutionId, JobStage jobStage) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobStage);
    }
}
