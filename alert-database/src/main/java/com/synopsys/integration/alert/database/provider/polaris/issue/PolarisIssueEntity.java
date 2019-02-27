package com.synopsys.integration.alert.database.provider.polaris.issue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "polaris_issues")
public class PolarisIssueEntity extends DatabaseEntity {
    @Column(name = "issue_type")
    private String issueType;
    @Column(name = "previous_count")
    private Integer previousCount;
    @Column(name = "current_count")
    private Integer currentCount;
    @Column(name = "project_id")
    private Long projectId;

    public PolarisIssueEntity() {
        // JPA requires default constructor definitions
    }

    public PolarisIssueEntity(final String issueType, final Integer previousCount, final Integer currentCount, final Long projectId) {
        this.issueType = issueType;
        this.previousCount = previousCount;
        this.currentCount = currentCount;
        this.projectId = projectId;
    }

    public String getIssueType() {
        return issueType;
    }

    public Integer getPreviousCount() {
        return previousCount;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public Long getProjectId() {
        return projectId;
    }
}
