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
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueTrackerChannel extends DistributionChannel implements ProviderCallbackEventProducer {
    private final IssueTrackerChannelKey channelKey;
    private final EventManager eventManager;

    public IssueTrackerChannel(Gson gson, AuditAccessor auditAccessor, IssueTrackerChannelKey channelKey, EventManager eventManager) {
        super(gson, auditAccessor);
        this.channelKey = channelKey;
        this.eventManager = eventManager;
    }

    @Override
    public final MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        IssueTrackerContext context = getIssueTrackerContext(event);
        List<IssueTrackerRequest> requests = createRequests(context, event);
        String statusMessage;
        if (requests.isEmpty()) {
            statusMessage = String.format("No requests to send to issue tracker: %s", channelKey.getDisplayName());
        } else {
            IssueTrackerResponse result = sendRequests(context, requests);
            statusMessage = result.getStatusMessage();

            List<ProviderCallbackEvent> callbackEvents = createCallbackEvents(result);
            sendProviderCallbackEvents(callbackEvents);
        }
        return new MessageResult(statusMessage);
    }

    @Override
    public final String getDestinationName() {
        return channelKey.getUniversalKey();
    }

    @Override
    public final void sendProviderCallbackEvents(List<ProviderCallbackEvent> callbackEvents) {
        eventManager.sendEvents(callbackEvents);
    }

    /**
     * This method will send requests to an Issue Tracker to create, update, or resolve issues.
     * @param context  The object containing the configuration of the issue tracker server and the configuration of how to map and manage issues.
     * @param requests The list of requests to submit to the issue tracker.  Must be a list because the order requests are added matter.
     * @return A response object containing the aggregate status of sending the requests passed.
     * @throws IntegrationException
     */
    public abstract IssueTrackerResponse sendRequests(IssueTrackerContext context, List<IssueTrackerRequest> requests) throws IntegrationException;

    protected abstract IssueTrackerContext getIssueTrackerContext(DistributionEvent event);

    protected abstract List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException;

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
