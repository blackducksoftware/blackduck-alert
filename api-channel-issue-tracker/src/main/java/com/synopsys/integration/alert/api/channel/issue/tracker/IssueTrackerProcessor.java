/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectMessage;

public class IssueTrackerProcessor<T extends Serializable> {
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerAsyncMessageSender<T> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerAsyncMessageSender<T> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

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
