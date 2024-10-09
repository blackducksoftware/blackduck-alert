package com.blackduck.integration.alert.common.persistence.model.job;

import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class SimpleFilteredDistributionJobResponseModel extends AlertSerializableModel {
    private static final long serialVersionUID = 6998968757605919978L;
    private final Long notificationId;
    private final UUID jobId;

    private final boolean filterByProject;
    private final boolean projectsConfigured;
    private final String projectNamePattern;
    private final String projectVersionNamePattern;

    public SimpleFilteredDistributionJobResponseModel(
        Long notificationId,
        UUID jobId,
        boolean filterByProject,
        String projectNamePattern,
        String projectVersionNamePattern,
        int numberOfConfiguredProjects
    ) {
        this(notificationId, jobId, filterByProject, projectNamePattern, projectVersionNamePattern, numberOfConfiguredProjects > 0);
    }

    public SimpleFilteredDistributionJobResponseModel(
        Long notificationId,
        UUID jobId,
        boolean filterByProject,
        String projectNamePattern,
        String projectVersionNamePattern,
        boolean projectsConfigured
    ) {
        this.notificationId = notificationId;
        this.jobId = jobId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
        this.projectsConfigured = projectsConfigured;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public boolean isFilterByProject() {
        return filterByProject;
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
