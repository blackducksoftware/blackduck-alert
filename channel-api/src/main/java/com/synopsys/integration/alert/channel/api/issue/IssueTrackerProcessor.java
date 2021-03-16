/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class IssueTrackerProcessor<T extends Serializable> {
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerMessageSender<T> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerMessageSender<T> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    public final IssueTrackerResponse processMessages(ProviderMessageHolder messages) throws AlertException {
        List<IssueTrackerIssueResponseModel> issueResponseModels = new LinkedList<>();

        IssueTrackerModelHolder<T> simpleMessageHolder = modelExtractor.extractSimpleMessageIssueModels(messages.getSimpleMessages());
        List<IssueTrackerIssueResponseModel> simpleMessageResponseModels = messageSender.sendMessages(simpleMessageHolder);
        issueResponseModels.addAll(simpleMessageResponseModels);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<T> projectMessageHolder = modelExtractor.extractProjectMessageIssueModels(projectMessage);
            List<IssueTrackerIssueResponseModel> projectMessageResponseModels = messageSender.sendMessages(projectMessageHolder);
            issueResponseModels.addAll(projectMessageResponseModels);
        }

        return new IssueTrackerResponse("Success", issueResponseModels);
    }

}
