/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;

public class IssueComponentUnknownVersionDetails extends AlertSerializableModel {
    private static final long serialVersionUID = 1732500677842103827L;
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
