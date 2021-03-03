/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class AzureBoardsJobDetailsModel extends DistributionJobDetailsModel {
    private final boolean addComments;
    private final String projectNameOrId;
    private final String workItemType;
    private final String workItemCompletedState;
    private final String workItemReopenState;

    public AzureBoardsJobDetailsModel(boolean addComments, String projectNameOrId, String workItemType, String workItemCompletedState, String workItemReopenState) {
        super(ChannelKey.AZURE_BOARDS);
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
