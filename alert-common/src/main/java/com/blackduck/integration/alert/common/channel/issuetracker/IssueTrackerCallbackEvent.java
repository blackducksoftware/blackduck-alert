/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.channel.issuetracker;

import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class IssueTrackerCallbackEvent extends AlertEvent {
    private final IssueTrackerCallbackInfo callbackInfo;

    private final String issueKey;
    private final String issueSummary;
    private final String issueUrl;
    private final IssueOperation operation;

    public IssueTrackerCallbackEvent(
        IssueTrackerCallbackInfo callbackInfo,
        String issueKey,
        String issueUrl,
        IssueOperation operation,
        String issueSummary
    ) {
        super(IssueTrackerCallbackEventListener.ISSUE_TRACKER_CALLBACK_DESTINATION_NAME);
        this.callbackInfo = callbackInfo;
        this.issueKey = issueKey;
        this.issueUrl = issueUrl;
        this.operation = operation;
        this.issueSummary = issueSummary;
    }

    public IssueTrackerCallbackInfo getCallbackInfo() {
        return callbackInfo;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public IssueOperation getOperation() {
        return operation;
    }

}
