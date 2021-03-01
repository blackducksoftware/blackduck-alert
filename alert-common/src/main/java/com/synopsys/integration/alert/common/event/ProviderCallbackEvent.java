/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

import java.util.Optional;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public class ProviderCallbackEvent extends AlertEvent {
    private final String callbackUrl;

    private final String issueId;
    private final String issueUrl;

    private final IssueOperation operation;
    private final String issueSummary;

    private final Long providerConfigId;
    private final String blackDuckProjectName;
    private final String blackDuckProjectVersionName;

    public ProviderCallbackEvent(
        ProviderKey providerKey,
        String callbackUrl,
        String issueId,
        String issueUrl,
        IssueOperation operation,
        String issueSummary,
        Long providerConfigId,
        String blackDuckProjectName,
        String blackDuckProjectVersionName
    ) {
        super(providerKey.getUniversalKey());
        this.callbackUrl = callbackUrl;
        this.issueId = issueId;
        this.issueUrl = issueUrl;
        this.operation = operation;
        this.issueSummary = issueSummary;
        this.providerConfigId = providerConfigId;
        this.blackDuckProjectName = blackDuckProjectName;
        this.blackDuckProjectVersionName = blackDuckProjectVersionName;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getIssueId() {
        return issueId;
    }

    public Optional<String> getIssueUrl() {
        return Optional.ofNullable(issueUrl);
    }

    public IssueOperation getOperation() {
        return operation;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    public String getBlackDuckProjectVersionName() {
        return blackDuckProjectVersionName;
    }

}
