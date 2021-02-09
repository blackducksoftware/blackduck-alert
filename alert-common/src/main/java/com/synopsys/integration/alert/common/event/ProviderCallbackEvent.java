/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.provider.ProviderKey;

public class ProviderCallbackEvent extends AlertEvent {
    private final String callbackUrl;
    private final String notificationType;

    private final String channelDestinationId;
    private final String channelDestinationLink;

    private final IssueOperation operation;
    private final String channelActionSummary;

    private final ContentKey providerContentKey;
    private final ComponentItem componentItem;

    public ProviderCallbackEvent(
        ProviderKey providerKey,
        String callbackUrl,
        String notificationType,
        String channelDestinationId,
        String channelDestinationLink,
        IssueOperation operation,
        String channelActionSummary,
        ContentKey providerContentKey,
        ComponentItem componentItem
    ) {
        super(providerKey.getUniversalKey());
        this.callbackUrl = callbackUrl;
        this.notificationType = notificationType;
        this.channelDestinationId = channelDestinationId;
        this.channelDestinationLink = channelDestinationLink;
        this.operation = operation;
        this.channelActionSummary = channelActionSummary;
        this.providerContentKey = providerContentKey;
        this.componentItem = componentItem;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getChannelDestinationId() {
        return channelDestinationId;
    }

    public Optional<String> getChannelDestinationLink() {
        return Optional.ofNullable(channelDestinationLink);
    }

    public IssueOperation getOperation() {
        return operation;
    }

    public String getChannelActionSummary() {
        return channelActionSummary;
    }

    public ContentKey getProviderContentKey() {
        return providerContentKey;
    }

    public ComponentItem getComponentItem() {
        return componentItem;
    }

}
