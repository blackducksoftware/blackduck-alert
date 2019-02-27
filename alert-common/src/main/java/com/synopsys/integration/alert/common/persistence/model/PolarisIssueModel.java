package com.synopsys.integration.alert.common.persistence.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class PolarisIssueModel extends AlertSerializableModel {
    private final String issueType;
    private final Integer previousIssueCount;
    private final Integer currentIssueCount;

    public PolarisIssueModel(final String issueType, final Integer previousIssueCount, final Integer currentIssueCount) {
        this.issueType = issueType;
        this.previousIssueCount = previousIssueCount;
        this.currentIssueCount = currentIssueCount;
    }

    public String getIssueType() {
        return issueType;
    }

    public Integer getPreviousIssueCount() {
        return previousIssueCount;
    }

    public Integer getCurrentIssueCount() {
        return currentIssueCount;
    }

    public boolean isIssueCountIncreasing() {
        return previousIssueCount < currentIssueCount;
    }

    public boolean isIssueCountDecreasing() {
        return !isIssueCountIncreasing() && previousIssueCount != currentIssueCount;
    }

}
