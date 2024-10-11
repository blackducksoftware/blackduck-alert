/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

import java.util.UUID;

public class JobStageEndedEvent extends JobStageEvent {
    private static final long serialVersionUID = -5480882643467010869L;
    public static final String DEFAULT_DESTINATION_NAME = "job_stage_end_event";

    public long endTimeMilliseconds;

    public JobStageEndedEvent(UUID jobExecutionId, JobStage jobStage, long endTimeMilliseconds) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobStage);
        this.endTimeMilliseconds = endTimeMilliseconds;
    }

    public long getEndTimeMilliseconds() {
        return endTimeMilliseconds;
    }
}
