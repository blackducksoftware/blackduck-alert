/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTrackerIssueResponseModel extends AlertSerializableModel {
    private final AlertIssueOrigin alertIssueOrigin;
    private final String issueKey;
    private final String issueLink;
    private final String issueTitle;
    private final IssueOperation issueOperation;

    public IssueTrackerIssueResponseModel(AlertIssueOrigin alertIssueOrigin, String issueKey, String issueLink, String issueTitle, IssueOperation issueOperation) {
        this.alertIssueOrigin = alertIssueOrigin;
        this.issueKey = issueKey;
        this.issueLink = issueLink;
        this.issueTitle = issueTitle;
        this.issueOperation = issueOperation;
    }

    public AlertIssueOrigin getAlertIssueOrigin() {
        return alertIssueOrigin;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueLink() {
        return issueLink;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public IssueOperation getIssueOperation() {
        return issueOperation;
    }

}
