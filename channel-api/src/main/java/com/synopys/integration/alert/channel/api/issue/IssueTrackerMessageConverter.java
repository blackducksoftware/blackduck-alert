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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopys.integration.alert.channel.api.convert.ChannelMessageConverter;
import com.synopys.integration.alert.channel.api.convert.ProjectMessageConverter;
import com.synopys.integration.alert.channel.api.convert.SimpleMessageConverter;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.IssueTrackerMessageHolder;

public abstract class IssueTrackerMessageConverter<D extends DistributionJobDetailsModel, T extends Serializable> implements ChannelMessageConverter<D, IssueTrackerMessageHolder<T>> {
    private final IssueTrackerMessageFormatter messageFormatter;
    private final SimpleMessageConverter simpleMessageConverter;
    private final ProjectMessageConverter projectMessageConverter;
    private final IssueTrackerSearcher<T> issueTrackerSearcher;

    protected IssueTrackerMessageConverter(IssueTrackerMessageFormatter messageFormatter, IssueTrackerSearcher<T> issueTrackerSearcher) {
        this.messageFormatter = messageFormatter;
        this.simpleMessageConverter = new SimpleMessageConverter(messageFormatter);
        this.projectMessageConverter = new ProjectMessageConverter(messageFormatter);
        this.issueTrackerSearcher = issueTrackerSearcher;
    }

    @Override
    public final List<IssueTrackerMessageHolder<T>> convertToChannelMessages(D distributionDetails, ProviderMessageHolder messages) {
        List<IssueTrackerMessageHolder<T>> issueTrackerChannelMessages = new LinkedList<>();

        List<SimpleMessage> simpleMessages = messages.getSimpleMessages();
        List<IssueCreationModel> simpleMessageIssueCreationModels = new ArrayList<>(simpleMessages.size());
        for (SimpleMessage simpleMessage : simpleMessages) {
            IssueCreationModel simpleMessageIssueCreationModel = convertSimpleMessageToCreationModel(distributionDetails, simpleMessage);
            simpleMessageIssueCreationModels.add(simpleMessageIssueCreationModel);
        }

        IssueTrackerMessageHolder<T> simpleMessageHolder = new IssueTrackerMessageHolder<>(simpleMessageIssueCreationModels, List.of(), List.of());
        issueTrackerChannelMessages.add(simpleMessageHolder);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerMessageHolder<T> projectMessagesHolder = convertProjectMessage(distributionDetails, projectMessage);
            issueTrackerChannelMessages.add(projectMessagesHolder);
        }

        return issueTrackerChannelMessages;
    }

    protected abstract IssueCreationModel convertSimpleMessageToCreationModel(D distributionDetails, SimpleMessage simpleMessage);

    protected abstract IssueCreationModel convertProjectMessageToCreationModel(D distributionDetails, ProjectMessage projectMessage, ComponentConcern targetConcern);

    private IssueTrackerMessageHolder<T> convertProjectMessage(D distributionDetails, ProjectMessage projectMessage) {
        IssueTrackerMessageHolder<T> combinedResults = new IssueTrackerMessageHolder<>(List.of(), List.of(), List.of());
        List<IssueSearchResult<T>> searchResults = issueTrackerSearcher.findIssues(projectMessage);
        for (IssueSearchResult<T> searchResult : searchResults) {
            IssueTrackerMessageHolder<T> searchResultMessages = convertSearchResult(distributionDetails, projectMessage, searchResult);
            combinedResults = IssueTrackerMessageHolder.reduce(combinedResults, searchResultMessages);
        }
        return combinedResults;
    }

    private IssueTrackerMessageHolder<T> convertSearchResult(D distributionDetails, ProjectMessage projectMessage, IssueSearchResult<T> searchResult) {
        Optional<T> optionalIssueId = searchResult.getIssueId();
        ComponentConcern componentConcern = searchResult.getComponentConcern();
        if (optionalIssueId.isPresent()) {
            return convertExistingIssue(distributionDetails, projectMessage, optionalIssueId.get(), componentConcern);
        } else {
            IssueCreationModel issueCreationModel = convertProjectMessageToCreationModel(distributionDetails, projectMessage, componentConcern);
            return new IssueTrackerMessageHolder<>(List.of(issueCreationModel), List.of(), List.of());
        }
    }

    private IssueTrackerMessageHolder<T> convertExistingIssue(D distributionDetails, ProjectMessage projectMessage, T issueId, ComponentConcern targetConcern) {
        // FIXME implement
        return new IssueTrackerMessageHolder<>(List.of(), List.of(), List.of());
    }

}
