/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import java.util.UUID;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class JobStageEvent extends AlertEvent {
    private static final long serialVersionUID = 7484019815048606767L;
    private final UUID jobExecutionId;
    private final JobStage jobStage;

    public JobStageEvent(String destination, UUID jobExecutionId, JobStage jobStage) {
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
