/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job;

import java.util.UUID;

public class FilteredDistributionJob {
    private final UUID jobId;
    private final Long notificationId;

    private final Boolean filterByProject;
    private final String projectNamePattern;
    private final String projectVersionNamePattern;

    public FilteredDistributionJob(
        UUID jobId,
        Long notificationId,
        Boolean filterByProject,
        String projectNamePattern,
        String projectVersionNamePattern
    ) {
        this.jobId = jobId;
        this.notificationId = notificationId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public String getProjectVersionNamePattern() {
        return projectVersionNamePattern;
    }
}
