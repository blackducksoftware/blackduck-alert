/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.channel.api.issue.convert.IssueTrackerMessageFormatter;
import com.synopsys.integration.alert.channel.api.issue.convert.IssueTrackerSimpleMessageConverter;
import com.synopsys.integration.alert.channel.api.issue.convert.ProjectIssueModelConverter;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ActionableIssueSearchResult;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

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
