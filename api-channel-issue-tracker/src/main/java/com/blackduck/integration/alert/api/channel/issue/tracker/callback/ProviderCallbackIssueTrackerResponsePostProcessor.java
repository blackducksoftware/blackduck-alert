/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.callback;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEvent;

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
