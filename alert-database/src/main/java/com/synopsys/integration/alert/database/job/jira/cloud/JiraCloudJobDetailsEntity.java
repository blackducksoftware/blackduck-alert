/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.jira.cloud;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldEntity;

@Entity
@Table(schema = "alert", name = "jira_cloud_job_details")
public class JiraCloudJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "add_comments")
    private Boolean addComments;

    @Column(name = "issue_creator_email")
    private String issueCreatorEmail;

    @Column(name = "project_name_or_key")
    private String projectNameOrKey;

    @Column(name = "issue_type")
    private String issueType;

    @Column(name = "resolve_transition")
    private String resolveTransition;

    @Column(name = "reopen_transition")
    private String reopenTransition;

    @Column(name = "issue_summary")
    private String issueSummary;

    @OneToMany
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private List<JiraCloudJobCustomFieldEntity> jobCustomFields;

    public JiraCloudJobDetailsEntity() {
    }

    public JiraCloudJobDetailsEntity(UUID jobId, Boolean addComments, String issueCreatorEmail, String projectNameOrKey, String issueType, String resolveTransition, String reopenTransition, String issueSummary) {
        this.jobId = jobId;
        this.addComments = addComments;
        this.issueCreatorEmail = issueCreatorEmail;
        this.projectNameOrKey = projectNameOrKey;
        this.issueType = issueType;
        this.resolveTransition = resolveTransition;
        this.reopenTransition = reopenTransition;
        this.issueSummary = issueSummary;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Boolean getAddComments() {
        return addComments;
    }

    public void setAddComments(Boolean addComments) {
        this.addComments = addComments;
    }

    public String getIssueCreatorEmail() {
        return issueCreatorEmail;
    }

    public void setIssueCreatorEmail(String issueCreatorEmail) {
        this.issueCreatorEmail = issueCreatorEmail;
    }

    public String getProjectNameOrKey() {
        return projectNameOrKey;
    }

    public void setProjectNameOrKey(String projectNameOrKey) {
        this.projectNameOrKey = projectNameOrKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getResolveTransition() {
        return resolveTransition;
    }

    public void setResolveTransition(String resolveTransition) {
        this.resolveTransition = resolveTransition;
    }

    public String getReopenTransition() {
        return reopenTransition;
    }

    public void setReopenTransition(String reopenTransition) {
        this.reopenTransition = reopenTransition;
    }

    public List<JiraCloudJobCustomFieldEntity> getJobCustomFields() {
        return jobCustomFields;
    }

    public void setJobCustomFields(List<JiraCloudJobCustomFieldEntity> jobCustomFields) {
        this.jobCustomFields = jobCustomFields;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

}
