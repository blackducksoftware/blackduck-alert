/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.util.List;

public class IssueComponentUnknownVersionDetails {
    private List<IssueEstimatedRiskModel> estimatedRiskModelList;

    public IssueComponentUnknownVersionDetails(List<IssueEstimatedRiskModel> estimatedRiskModelList) {
        this.estimatedRiskModelList = estimatedRiskModelList;
    }

    public List<IssueEstimatedRiskModel> getEstimatedRiskModelList() {
        return estimatedRiskModelList;
    }
}
