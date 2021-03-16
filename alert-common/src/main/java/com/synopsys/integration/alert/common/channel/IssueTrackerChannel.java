/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.IssueTrackerCallbackEvent;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;
import com.synopsys.integration.exception.IntegrationException;

@Deprecated
public abstract class IssueTrackerChannel {
    private final IssueTrackerChannelKey channelKey;
    private final EventManager eventManager;

    public IssueTrackerChannel(IssueTrackerChannelKey channelKey, EventManager eventManager) {
        super();
        this.channelKey = channelKey;
        this.eventManager = eventManager;
    }

    public final MessageResult sendMessage(DistributionEvent event) throws IntegrationException {
        IssueTrackerContext context = getIssueTrackerContext(event);
        List<IssueTrackerRequest> requests = createRequests(context, event);
        String statusMessage;
        if (requests.isEmpty()) {
            statusMessage = String.format("No requests to send to issue tracker: %s", channelKey.getDisplayName());
        } else {
            IssueTrackerResponse result = sendRequests(context, requests);
            statusMessage = result.getStatusMessage();

            List<IssueTrackerCallbackEvent> callbackEvents = createCallbackEvents(result);
            sendProviderCallbackEvents(callbackEvents);
        }
        return new MessageResult(statusMessage);
    }

    public final void sendProviderCallbackEvents(List<IssueTrackerCallbackEvent> callbackEvents) {
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

    protected abstract IssueTrackerContext getIssueTrackerContext(DistributionEvent event) throws AlertConfigurationException;

    protected abstract List<IssueTrackerRequest> createRequests(IssueTrackerContext context, DistributionEvent event) throws IntegrationException;

    private List<IssueTrackerCallbackEvent> createCallbackEvents(IssueTrackerResponse issueTrackerResponse) {
        Collection<IssueTrackerIssueResponseModel> updatedIssues = issueTrackerResponse.getUpdatedIssues();
        List<IssueTrackerCallbackEvent> callbackEvents = new ArrayList<>(updatedIssues.size());
        for (IssueTrackerIssueResponseModel updatedIssue : updatedIssues) {
            createCallbackEvent(updatedIssue).ifPresent(callbackEvents::add);
        }
        return callbackEvents;
    }

    private Optional<IssueTrackerCallbackEvent> createCallbackEvent(IssueTrackerIssueResponseModel issueResponseModel) {
        return issueResponseModel.getCallbackInfo()
                   .map(callbackInfo ->
                            new IssueTrackerCallbackEvent(
                                callbackInfo,
                                issueResponseModel.getIssueKey(),
                                issueResponseModel.getIssueLink(),
                                issueResponseModel.getIssueOperation(),
                                issueResponseModel.getIssueTitle()
                            )
                   );
    }

}
