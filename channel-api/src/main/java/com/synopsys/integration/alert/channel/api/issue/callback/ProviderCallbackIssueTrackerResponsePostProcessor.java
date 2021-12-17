/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.callback;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.common.event.IssueTrackerCallbackEvent;
import com.synopsys.integration.alert.common.workflow.IssueTrackerCallbackHandler;

@Component
public class ProviderCallbackIssueTrackerResponsePostProcessor implements IssueTrackerResponsePostProcessor {
    private final IssueTrackerCallbackHandler handler;

    @Autowired
    public ProviderCallbackIssueTrackerResponsePostProcessor(IssueTrackerCallbackHandler handler) {
        this.handler = handler;
    }

    @Override
    public <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        List<IssueTrackerCallbackEvent> callbackEvents = createCallbackEvents(response);
        // This code is executed from inside an event handler for an issue tracker channel.
        // In the short term we cannot post an event from inside an event handler because it is a consumer of events.
        // no memory can be freed for other consumers to run since a consumer is blocked trying to produce an event onto a queue.
        // call the handler directly for now which will make the same post-processing calls in the same thread then the consumer thread should free up.
        for (IssueTrackerCallbackEvent event : callbackEvents) {
            handler.handleEvent(event);
        }
    }

    private <T extends Serializable> List<IssueTrackerCallbackEvent> createCallbackEvents(IssueTrackerResponse<T> issueTrackerResponse) {
        return issueTrackerResponse.getUpdatedIssues()
                   .stream()
                   .map(this::createProviderCallbackEvent)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private <T extends Serializable> Optional<IssueTrackerCallbackEvent> createProviderCallbackEvent(IssueTrackerIssueResponseModel<T> issueResponseModel) {
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
