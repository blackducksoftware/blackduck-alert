/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

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
