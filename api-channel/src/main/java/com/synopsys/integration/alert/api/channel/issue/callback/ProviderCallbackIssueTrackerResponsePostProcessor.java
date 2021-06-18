/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.callback;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEvent;

@Component
public class ProviderCallbackIssueTrackerResponsePostProcessor implements IssueTrackerResponsePostProcessor {
    private final EventManager eventManager;

    @Autowired
    public ProviderCallbackIssueTrackerResponsePostProcessor(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
        List<IssueTrackerCallbackEvent> callbackEvents = createCallbackEvents(response);
        eventManager.sendEvents(callbackEvents);
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
