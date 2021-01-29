/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class AzureBoardsJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String projectNameOrId;
    private final String workItemType;
    private final String workItemCompletedState;
    private final String workItemReopenState;

    public AzureBoardsJobDetailsModel(UUID jobId, boolean addComments, String projectNameOrId, String workItemType, String workItemCompletedState, String workItemReopenState) {
        super(ChannelKeys.AZURE_BOARDS, jobId);
        this.addComments = addComments;
        this.projectNameOrId = projectNameOrId;
        this.workItemType = workItemType;
        this.workItemCompletedState = workItemCompletedState;
        this.workItemReopenState = workItemReopenState;
    }

    public boolean isAddComments() {
        return addComments;
    }

    public String getProjectNameOrId() {
        return projectNameOrId;
    }

    public String getWorkItemType() {
        return workItemType;
    }

    public String getWorkItemCompletedState() {
        return workItemCompletedState;
    }

    public String getWorkItemReopenState() {
        return workItemReopenState;
    }

}
