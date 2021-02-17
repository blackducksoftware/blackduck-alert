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

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopys.integration.alert.channel.api.issue.model.ActionableIssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;

public class IssueTrackerModelExtractor<T extends Serializable> {
    private final IssueTrackerSimpleMessageConverter issueTrackerSimpleMessageConverter;
    private final ProjectIssueModelConverter projectIssueModelConverter;
    private final IssueTrackerSearcher<T> issueTrackerSearcher;

    public IssueTrackerModelExtractor(IssueTrackerMessageFormatter formatter, IssueTrackerSearcher<T> issueTrackerSearcher) {
        this.issueTrackerSimpleMessageConverter = new IssueTrackerSimpleMessageConverter(formatter);
        this.projectIssueModelConverter = new ProjectIssueModelConverter(formatter);
        this.issueTrackerSearcher = issueTrackerSearcher;
    }

    public final List<IssueTrackerModelHolder<T>> extractIssueTrackerModels(ProviderMessageHolder messages) throws AlertException {
        List<IssueTrackerModelHolder<T>> issueTrackerModels = new LinkedList<>();

        List<SimpleMessage> simpleMessages = messages.getSimpleMessages();
        List<IssueCreationModel> simpleMessageIssueCreationModels = new ArrayList<>(simpleMessages.size());
        for (SimpleMessage simpleMessage : simpleMessages) {
            IssueCreationModel simpleMessageIssueCreationModel = issueTrackerSimpleMessageConverter.convertToIssueCreationModel(simpleMessage);
            simpleMessageIssueCreationModels.add(simpleMessageIssueCreationModel);
        }

        IssueTrackerModelHolder<T> simpleMessageHolder = new IssueTrackerModelHolder<>(simpleMessageIssueCreationModels, List.of(), List.of());
        issueTrackerModels.add(simpleMessageHolder);

        for (ProjectMessage projectMessage : messages.getProjectMessages()) {
            IssueTrackerModelHolder<T> projectMessagesHolder = convertProjectMessage(projectMessage);
            issueTrackerModels.add(projectMessagesHolder);
        }

        return issueTrackerModels;
    }

    private IssueTrackerModelHolder<T> convertProjectMessage(ProjectMessage projectMessage) throws AlertException {
        IssueTrackerModelHolder<T> combinedResults = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());
        List<ActionableIssueSearchResult<T>> searchResults = issueTrackerSearcher.findIssues(projectMessage);
        for (ActionableIssueSearchResult<T> searchResult : searchResults) {
            IssueTrackerModelHolder<T> searchResultMessages = convertSearchResult(searchResult);
            combinedResults = IssueTrackerModelHolder.reduce(combinedResults, searchResultMessages);
        }
        return combinedResults;
    }

    private IssueTrackerModelHolder<T> convertSearchResult(ActionableIssueSearchResult<T> searchResult) {
        Optional<ExistingIssueDetails<T>> existingIssueDetails = searchResult.getExistingIssueDetails();
        ProjectIssueModel projectIssueModel = searchResult.getProjectIssueModel();
        if (existingIssueDetails.isPresent()) {
            return convertExistingIssue(existingIssueDetails.get(), projectIssueModel, searchResult.getRequiredOperation());
        } else {
            IssueCreationModel issueCreationModel = projectIssueModelConverter.toIssueCreationModel(projectIssueModel);
            return new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        }
    }

    private IssueTrackerModelHolder<T> convertExistingIssue(ExistingIssueDetails<T> existingIssueDetails, ProjectIssueModel projectIssueModel, ItemOperation requiredOperation) {
        List<IssueTransitionModel<T>> transitionModels = new ArrayList<>(1);
        List<IssueCommentModel<T>> commentModels = new ArrayList<>(1);
        if (ItemOperation.UPDATE.equals(requiredOperation) || ItemOperation.INFO.equals(requiredOperation)) {
            IssueCommentModel<T> projectIssueCommentModel = projectIssueModelConverter.toIssueCommentModel(existingIssueDetails, projectIssueModel);
            commentModels.add(projectIssueCommentModel);
        } else {
            IssueTransitionModel<T> projectIssueTransitionModel = projectIssueModelConverter.toIssueTransitionModel(existingIssueDetails, projectIssueModel, requiredOperation);
            transitionModels.add(projectIssueTransitionModel);
        }
        return new IssueTrackerModelHolder<>(List.of(), transitionModels, commentModels);
    }

}
