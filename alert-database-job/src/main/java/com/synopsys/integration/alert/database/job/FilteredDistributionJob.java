/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job;

import java.util.UUID;

public class FilteredDistributionJob {
    private final UUID jobId;
    private final Long notificationId;
    private final String projectNamePattern;
    private final String projectVersionNamePattern;

    public FilteredDistributionJob(UUID jobId, Long notificationId, String projectNamePattern, String projectVersionNamePattern) {
        this.jobId = jobId;
        this.notificationId = notificationId;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public String getProjectVersionNamePattern() {
        return projectVersionNamePattern;
    }
}
