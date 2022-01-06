/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
