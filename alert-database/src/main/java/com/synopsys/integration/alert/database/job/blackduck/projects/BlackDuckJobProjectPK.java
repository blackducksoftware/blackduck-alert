package com.synopsys.integration.alert.database.job.blackduck.projects;

import java.io.Serializable;
import java.util.UUID;

public class BlackDuckJobProjectPK implements Serializable {
    private UUID jobId;
    private String projectName;

    public BlackDuckJobProjectPK() {
    }

    public BlackDuckJobProjectPK(UUID jobId, String projectName) {
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

}
