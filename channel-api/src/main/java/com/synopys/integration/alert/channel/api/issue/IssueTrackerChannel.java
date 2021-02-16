/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopys.integration.alert.channel.api.DistributionChannelV2;
import com.synopys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;

/**
 * @param <D> The type of job details relevant to this channel.
 * @param <T> The {@link Serializable} type of an issue-tracker issue's ID.
 */
public abstract class IssueTrackerChannel<D extends DistributionJobDetailsModel, T extends Serializable> implements DistributionChannelV2<D> {
    private final IssueTrackerResponsePostProcessor responsePostProcessor;

    protected IssueTrackerChannel(IssueTrackerResponsePostProcessor responsePostProcessor) {
        this.responsePostProcessor = responsePostProcessor;
    }

    @Override
    public MessageResult distributeMessages(D distributionDetails, ProviderMessageHolder messages) throws AlertException {
        IssueTrackerModelExtractor<T> modelExtractor = createModelExtractor(distributionDetails);
        List<IssueTrackerModelHolder<T>> channelMessages = modelExtractor.extractIssueTrackerModels(messages);

        IssueTrackerMessageSender<D, T> messageSender = createMessageSender(distributionDetails);
        IssueTrackerResponse issueTrackerResponse = messageSender.sendMessages(distributionDetails, channelMessages);

        responsePostProcessor.postProcess(issueTrackerResponse);
        return new MessageResult(issueTrackerResponse.getStatusMessage());
    }

    protected abstract IssueTrackerModelExtractor<T> createModelExtractor(D distributionDetails);

    protected abstract IssueTrackerMessageSender<D, T> createMessageSender(D distributionDetails);

}
