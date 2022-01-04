/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class IssueTrackerIssueResponseModel<T extends Serializable> extends AlertSerializableModel {
    private final T issueId;
    private final String issueKey;
    private final String issueLink;
    private final String issueTitle;
    private final IssueOperation issueOperation;

    private final IssueTrackerCallbackInfo callbackInfo;

    public IssueTrackerIssueResponseModel(
        T issueId,
        String issueKey,
        String issueLink,
        String issueTitle,
        IssueOperation issueOperation,
        @Nullable IssueTrackerCallbackInfo callbackInfo
    ) {
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.issueLink = issueLink;
        this.issueTitle = issueTitle;
        this.issueOperation = issueOperation;
        this.callbackInfo = callbackInfo;
    }

    public T getIssueId() {
        return issueId;
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
