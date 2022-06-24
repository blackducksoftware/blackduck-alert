/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class SimpleFilteredDistributionJobResponseModel extends AlertSerializableModel {
    private static final long serialVersionUID = 6998968757605919978L;
    private final Long notificationId;
    private final UUID jobId;

    private final boolean projectsConfigured;
    private final String projectNamePattern;
    private final String projectVersionNamePattern;

    public SimpleFilteredDistributionJobResponseModel(
        Long notificationId,
        UUID jobId,
        int numberOfConfiguredProjects,
        String projectNamePattern,
        String projectVersionNamePattern
    ) {
        this(notificationId, jobId, numberOfConfiguredProjects > 0, projectNamePattern, projectVersionNamePattern);
    }

    public SimpleFilteredDistributionJobResponseModel(
        Long notificationId,
        UUID jobId,
        boolean projectsConfigured,
        String projectNamePattern,
        String projectVersionNamePattern
    ) {
        this.notificationId = notificationId;
        this.jobId = jobId;
        this.projectsConfigured = projectsConfigured;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public boolean hasProjectsConfigured() {
        return projectsConfigured;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public String getProjectVersionNamePattern() {
        return projectVersionNamePattern;
    }
}
