/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.callback;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEvent;

@Component
public class ProviderCallbackIssueTrackerResponsePostProcessor implements IssueTrackerResponsePostProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
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
            .toList();
    }

    private <T extends Serializable> Optional<IssueTrackerCallbackEvent> createProviderCallbackEvent(IssueTrackerIssueResponseModel<T> issueResponseModel) {
        return issueResponseModel.getCallbackInfo()
            .map(callbackInfo -> {
                logger.debug(
                    "Creating issue tracker callback event: issue key: [{}], issue operation: [{}], issue title: [{}], link: [{}]",
                    issueResponseModel.getIssueKey(),
                    issueResponseModel.getIssueOperation(),
                    issueResponseModel.getIssueTitle(),
                    issueResponseModel.getIssueLink()
                );
                return new IssueTrackerCallbackEvent(
                    callbackInfo,
                    issueResponseModel.getIssueKey(),
                    issueResponseModel.getIssueLink(),
                    issueResponseModel.getIssueOperation(),
                    issueResponseModel.getIssueTitle()
                );
            });
    }

}
