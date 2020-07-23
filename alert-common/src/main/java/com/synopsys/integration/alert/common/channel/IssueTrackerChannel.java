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
package com.synopsys.integration.alert.common.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueTrackerService;
import com.synopsys.integration.alert.common.channel.key.ChannelKey;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueTrackerChannel extends DistributionChannel implements ProviderCallbackEventProducer {
    private final ChannelKey channelKey;
    private final EventManager eventManager;

    public IssueTrackerChannel(Gson gson, AuditUtility auditUtility, ChannelKey channelKey, EventManager eventManager) {
        super(gson, auditUtility);
        this.channelKey = channelKey;
        this.eventManager = eventManager;
    }

    protected abstract IssueTrackerService getIssueTrackerService();

    protected abstract IssueTrackerContext getIssueTrackerContext(DistributionEvent event);

    protected abstract List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException;

    @Override
    public MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        IssueTrackerContext context = getIssueTrackerContext(event);
        IssueTrackerService service = getIssueTrackerService();
        List<IssueTrackerRequest> requests = createRequests(context, event);
        String statusMessage;
        if (requests.isEmpty()) {
            statusMessage = String.format("No requests to send to issue tracker: %s", channelKey.getDisplayName());
        } else {
            IssueTrackerResponse result = service.sendRequests(context, requests);
            statusMessage = result.getStatusMessage();

            List<ProviderCallbackEvent> callbackEvents = createCallbackEvents(result);
            sendProviderCallbackEvents(callbackEvents);
        }
        return new MessageResult(statusMessage);
    }

    @Override
    public String getDestinationName() {
        return channelKey.getUniversalKey();
    }

    @Override
    public final void sendProviderCallbackEvents(List<ProviderCallbackEvent> callbackEvents) {
        eventManager.sendEvents(callbackEvents);
    }

    private List<ProviderCallbackEvent> createCallbackEvents(IssueTrackerResponse issueTrackerResponse) {
        List<ProviderCallbackEvent> callbackEvents = new ArrayList<>();
        for (IssueTrackerIssueResponseModel issueResponseModel : issueTrackerResponse.getUpdatedIssues()) {
            AlertIssueOrigin alertIssueOrigin = issueResponseModel.getAlertIssueOrigin();

            Optional<ComponentItem> optionalComponentItem = alertIssueOrigin.getComponentItem();
            if (optionalComponentItem.isPresent()) {
                ComponentItem componentItem = optionalComponentItem.get();
                Optional<ComponentItemCallbackInfo> optionalCallbackInfo = componentItem.getCallbackInfo();
                if (optionalCallbackInfo.isPresent()) {
                    ComponentItemCallbackInfo callbackInfo = optionalCallbackInfo.get();
                    ProviderCallbackEvent issueCallback = new ProviderCallbackEvent(
                        callbackInfo.getProviderKey(),
                        callbackInfo.getCallbackUrl(),
                        callbackInfo.getNotificationType(),
                        issueResponseModel.getIssueKey(),
                        issueResponseModel.getIssueLink(),
                        issueResponseModel.getIssueOperation(),
                        issueResponseModel.getIssueTitle(),
                        alertIssueOrigin.getProviderContentKey(),
                        componentItem
                    );
                    callbackEvents.add(issueCallback);
                }
            }
        }
        return callbackEvents;
    }

}
