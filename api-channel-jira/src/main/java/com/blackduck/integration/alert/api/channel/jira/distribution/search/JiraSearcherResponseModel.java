/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

public class JiraSearcherResponseModel {
    private final String issueUrl;
    private final String issueKey;
    private final String issueId;
    private final String summaryField;

    public JiraSearcherResponseModel(String issueUrl, String issueKey, String issueId, String summaryField) {
        this.issueUrl = issueUrl;
        this.issueKey = issueKey;
        this.issueId = issueId;
        this.summaryField = summaryField;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getSummaryField() {
        return summaryField;
    }

}
