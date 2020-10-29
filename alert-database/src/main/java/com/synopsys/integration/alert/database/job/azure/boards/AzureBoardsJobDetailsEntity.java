/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
