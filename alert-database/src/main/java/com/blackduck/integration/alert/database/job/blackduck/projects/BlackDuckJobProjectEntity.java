/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.blackduck.projects;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(BlackDuckJobProjectPK.class)
@Table(schema = "alert", name = "blackduck_job_projects")
public class BlackDuckJobProjectEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "project_name")
    private String projectName;

    @Id
    @Column(name = "href")
    private String href;

    public BlackDuckJobProjectEntity() {
    }

    public BlackDuckJobProjectEntity(UUID jobId, String projectName, String href) {
        this.jobId = jobId;
        this.projectName = projectName;
        this.href = href;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
