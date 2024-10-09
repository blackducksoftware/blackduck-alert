/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerSimpleMessageConverter;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectIssueModelConverter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ActionableIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;

public class IssueTrackerModelExtractor<T extends Serializable> {
    private final IssueTrackerSimpleMessageConverter issueTrackerSimpleMessageConverter;
    private final ProjectIssueModelConverter projectIssueModelConverter;
    private final IssueTrackerSearcher<T> issueTrackerSearcher;

    public IssueTrackerModelExtractor(IssueTrackerMessageFormatter formatter, IssueTrackerSearcher<T> issueTrackerSearcher) {
        this.issueTrackerSimpleMessageConverter = new IssueTrackerSimpleMessageConverter(formatter);
        this.projectIssueModelConverter = new ProjectIssueModelConverter(formatter);
        this.issueTrackerSearcher = issueTrackerSearcher;
    }

    public final IssueTrackerModelHolder<T> extractSimpleMessageIssueModels(List<SimpleMessage> simpleMessages, String jobName) {
        List<IssueCreationModel> simpleMessageIssueCreationModels = new ArrayList<>(simpleMessages.size());
        for (SimpleMessage simpleMessage : simpleMessages) {
            IssueCreationModel simpleMessageIssueCreationModel = issueTrackerSimpleMessageConverter.convertToIssueCreationModel(simpleMessage, jobName);
            simpleMessageIssueCreationModels.add(simpleMessageIssueCreationModel);
        }

        return new IssueTrackerModelHolder<>(simpleMessageIssueCreationModels, List.of(), List.of());
    }

    public final IssueTrackerModelHolder<T> extractProjectMessageIssueModels(ProjectMessage projectMessage, String jobName) throws AlertException {
        IssueTrackerModelHolder<T> combinedResults = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());
        List<ActionableIssueSearchResult<T>> searchResults = issueTrackerSearcher.findIssues(projectMessage);
        for (ActionableIssueSearchResult<T> searchResult : searchResults) {
            IssueTrackerModelHolder<T> searchResultMessages = convertSearchResult(searchResult, jobName);
            combinedResults = IssueTrackerModelHolder.reduce(combinedResults, searchResultMessages);
        }
        return combinedResults;
    }

    private IssueTrackerModelHolder<T> convertSearchResult(ActionableIssueSearchResult<T> searchResult, String jobName) {
        Optional<ExistingIssueDetails<T>> existingIssueDetails = searchResult.getExistingIssueDetails();
        ProjectIssueModel projectIssueModel = searchResult.getProjectIssueModel();
        if (existingIssueDetails.isPresent()) {
            return convertExistingIssue(existingIssueDetails.get(), projectIssueModel, searchResult.getRequiredOperation());
        } else {
            IssueCreationModel issueCreationModel = projectIssueModelConverter.toIssueCreationModel(projectIssueModel, jobName, searchResult.getSearchQuery());
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
