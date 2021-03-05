/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTrackerIssueResponseModel extends AlertSerializableModel {
    private final String issueKey;
    private final String issueLink;
    private final String issueTitle;
    private final IssueOperation issueOperation;

    private final IssueTrackerCallbackInfo callbackInfo;

    public IssueTrackerIssueResponseModel(
        String issueKey,
        String issueLink,
        String issueTitle,
        IssueOperation issueOperation,
        @Nullable IssueTrackerCallbackInfo callbackInfo
    ) {
        this.issueKey = issueKey;
        this.issueLink = issueLink;
        this.issueTitle = issueTitle;
        this.issueOperation = issueOperation;
        this.callbackInfo = callbackInfo;
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

    public Optional<IssueTrackerCallbackInfo> getCallbackInfo() {
        return Optional.ofNullable(callbackInfo);
    }

}
