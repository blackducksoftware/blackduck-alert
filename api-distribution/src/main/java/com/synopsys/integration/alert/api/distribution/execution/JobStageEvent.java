package com.synopsys.integration.alert.api.distribution.execution;

import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class JobStageEvent extends AlertEvent {
    private static final long serialVersionUID = 7484019815048606767L;
    private final UUID jobExecutionId;
    private final JobStage jobStage;

    public JobStageEvent(final String destination, final UUID jobExecutionId, final JobStage jobStage) {
        super(destination);
        this.jobExecutionId = jobExecutionId;
        this.jobStage = jobStage;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public JobStage getJobStage() {
        return jobStage;
    }
}
