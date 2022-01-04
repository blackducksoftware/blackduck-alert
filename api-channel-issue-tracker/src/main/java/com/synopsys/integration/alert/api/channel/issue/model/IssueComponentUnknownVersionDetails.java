/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;

public class IssueComponentUnknownVersionDetails {
    private ItemOperation itemOperation;
    private List<IssueEstimatedRiskModel> estimatedRiskModelList;

    public IssueComponentUnknownVersionDetails(ItemOperation itemOperation, List<IssueEstimatedRiskModel> estimatedRiskModelList) {
        this.itemOperation = itemOperation;
        this.estimatedRiskModelList = estimatedRiskModelList;
    }

    public List<IssueEstimatedRiskModel> getEstimatedRiskModelList() {
        return estimatedRiskModelList;
    }

    public ItemOperation getItemOperation() {
        return itemOperation;
    }
}
