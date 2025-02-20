/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

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
