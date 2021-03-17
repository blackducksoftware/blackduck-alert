/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class IssueTrackerProcessor<T extends Serializable> {
    private final IssueTrackerModelExtractor<T> modelExtractor;
    private final IssueTrackerMessageSender<T> messageSender;

    public IssueTrackerProcessor(IssueTrackerModelExtractor<T> modelExtractor, IssueTrackerMessageSender<T> messageSender) {
        this.modelExtractor = modelExtractor;
        this.messageSender = messageSender;
    }

    public final IssueTrackerResponse processMessages(ProviderMessageHolder messages) throws AlertException {
        List<IssueTrackerModelHolder<T>> channelMessages = modelExtractor.extractIssueTrackerModels(messages);
        return messageSender.sendMessages(channelMessages);
    }

}
