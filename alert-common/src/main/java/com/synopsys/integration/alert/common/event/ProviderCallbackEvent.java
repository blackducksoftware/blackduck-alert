/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
