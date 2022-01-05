/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.projects;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
