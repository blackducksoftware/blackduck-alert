/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.azure.boards;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "alert", name = "azure_boards_job_details")
public class AzureBoardsJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "add_comments")
    private Boolean addComments;

    @Column(name = "project_name_or_id")
    private String projectNameOrId;

    @Column(name = "work_item_type")
    private String workItemType;

    @Column(name = "work_item_completed_state")
    private String workItemCompletedState;

    @Column(name = "work_item_reopen_state")
    private String workItemReopenState;

    public AzureBoardsJobDetailsEntity() {
    }

    public AzureBoardsJobDetailsEntity(UUID jobId, Boolean addComments, String projectNameOrId, String workItemType, String workItemCompletedState, String workItemReopenState) {
        this.jobId = jobId;
        this.addComments = addComments;
        this.projectNameOrId = projectNameOrId;
        this.workItemType = workItemType;
        this.workItemCompletedState = workItemCompletedState;
        this.workItemReopenState = workItemReopenState;
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

    public String getProjectNameOrId() {
        return projectNameOrId;
    }

    public void setProjectNameOrId(String projectNameOrId) {
        this.projectNameOrId = projectNameOrId;
    }

    public String getWorkItemType() {
        return workItemType;
    }

    public void setWorkItemType(String workItemType) {
        this.workItemType = workItemType;
    }

    public String getWorkItemCompletedState() {
        return workItemCompletedState;
    }

    public void setWorkItemCompletedState(String workItemCompletedState) {
        this.workItemCompletedState = workItemCompletedState;
    }

    public String getWorkItemReopenState() {
        return workItemReopenState;
    }

    public void setWorkItemReopenState(String workItemReopenState) {
        this.workItemReopenState = workItemReopenState;
    }

}
