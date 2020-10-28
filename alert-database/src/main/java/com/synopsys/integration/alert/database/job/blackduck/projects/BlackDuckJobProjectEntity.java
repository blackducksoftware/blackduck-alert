package com.synopsys.integration.alert.database.job.blackduck.projects;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;

@Entity
@IdClass(BlackDuckJobProjectPK.class)
@Table(schema = "alert", name = "blackduck_job_projects")
public class BlackDuckJobProjectEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "project_name")
    private String projectName;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private BlackDuckJobDetailsEntity blackDuckJobDetails;

    public BlackDuckJobProjectEntity() {
    }

    public BlackDuckJobProjectEntity(UUID jobId, String projectName) {
        this.jobId = jobId;
        this.projectName = projectName;
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

    public BlackDuckJobDetailsEntity getBlackDuckJobDetails() {
        return blackDuckJobDetails;
    }

    public void setBlackDuckJobDetails(BlackDuckJobDetailsEntity blackDuckJobDetails) {
        this.blackDuckJobDetails = blackDuckJobDetails;
    }

}
