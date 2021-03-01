/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.search;

import java.io.Serializable;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ExistingIssueDetails<T extends Serializable> extends AlertSerializableModel {
    private final T issueId;
    private final String issueKey;
    private final String issueSummary;
    private final String issueUILink;

    public ExistingIssueDetails(T issueId, String issueKey, String issueSummary, String issueUILink) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.issueSummary = issueSummary;
        this.issueUILink = issueUILink;
    }

    public T getIssueId() {
        return issueId;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getIssueUILink() {
        return issueUILink;
    }

}
