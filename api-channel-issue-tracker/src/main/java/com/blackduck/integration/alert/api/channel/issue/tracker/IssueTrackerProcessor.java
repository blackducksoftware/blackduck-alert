/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;

public class IssueTrackerProcessor<T extends Serializable> implements IssueTrackerMessageProcessor<T>{
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<T>> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<T>> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    @Override
    public final IssueTrackerResponse<T> processMessages(ProviderMessageHolder messages, String jobName) throws AlertException {
        List<IssueTrackerModelHolder<T>> issueTrackerModels = new LinkedList<>();
        IssueTrackerModelHolder<T> simpleMessageHolder = modelExtractor.extractSimpleMessageIssueModels(messages.getSimpleMessages(), jobName);
        issueTrackerModels.add(simpleMessageHolder);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<T> projectMessageHolder = modelExtractor.extractProjectMessageIssueModels(projectMessage, jobName);
            issueTrackerModels.add(projectMessageHolder);
        }

        messageSender.sendAsyncMessages(issueTrackerModels);

        return new IssueTrackerResponse<>("Success", List.of());
    }

}
