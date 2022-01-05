/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class IssueTrackerProcessor<T extends Serializable> {
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerMessageSender<T> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerMessageSender<T> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    public final IssueTrackerResponse<T> processMessages(ProviderMessageHolder messages, String jobName) throws AlertException {
        List<IssueTrackerIssueResponseModel<T>> issueResponseModels = new LinkedList<>();

        IssueTrackerModelHolder<T> simpleMessageHolder = modelExtractor.extractSimpleMessageIssueModels(messages.getSimpleMessages(), jobName);
        List<IssueTrackerIssueResponseModel<T>> simpleMessageResponseModels = messageSender.sendMessages(simpleMessageHolder);
        issueResponseModels.addAll(simpleMessageResponseModels);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<T> projectMessageHolder = modelExtractor.extractProjectMessageIssueModels(projectMessage, jobName);
            List<IssueTrackerIssueResponseModel<T>> projectMessageResponseModels = messageSender.sendMessages(projectMessageHolder);
            issueResponseModels.addAll(projectMessageResponseModels);
        }

        return new IssueTrackerResponse<>("Success", issueResponseModels);
    }

}
