/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event.distribution;

import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class JobSubTaskEvent extends AlertEvent {
    private static final long serialVersionUID = 2328435266614582583L;
    private final UUID jobExecutionId;
    private final UUID jobId;
    private final Set<Long> notificationIds;

    protected JobSubTaskEvent(String destination, UUID jobExecutionId, UUID jobId, Set<Long> notificationIds) {
        super(destination);
        this.jobExecutionId = jobExecutionId;
        this.jobId = jobId;
        this.notificationIds = notificationIds;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }
}
