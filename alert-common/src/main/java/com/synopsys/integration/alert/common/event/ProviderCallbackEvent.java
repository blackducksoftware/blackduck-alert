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
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public class ProviderCallbackEvent extends AlertEvent {
    private final String callbackUrl;
    private final String notificationType;

    private final String issueId;
    private final String issueUrl;

    private final IssueOperation operation;
    private final String issueSummary;

    private final ContentKey providerContentKey;
    private final ComponentItem componentItem;

    public ProviderCallbackEvent(
        ProviderKey providerKey,
        String callbackUrl,
        String notificationType,
        String issueId,
        String issueUrl,
        IssueOperation operation,
        String issueSummary,
        ContentKey providerContentKey,
        ComponentItem componentItem
    ) {
        super(providerKey.getUniversalKey());
        this.callbackUrl = callbackUrl;
        this.notificationType = notificationType;
        this.issueId = issueId;
        this.issueUrl = issueUrl;
        this.operation = operation;
        this.issueSummary = issueSummary;
        this.providerContentKey = providerContentKey;
        this.componentItem = componentItem;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getNotificationType() {
        return notificationType;
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

    public ContentKey getProviderContentKey() {
        return providerContentKey;
    }

    public ComponentItem getComponentItem() {
        return componentItem;
    }

}
