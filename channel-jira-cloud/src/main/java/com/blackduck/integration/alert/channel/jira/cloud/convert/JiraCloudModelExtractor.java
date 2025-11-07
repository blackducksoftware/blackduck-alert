package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ActionableIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JiraCloudModelExtractor {
    private final JiraCloudIssueTrackerSimpleMessageConverter issueTrackerSimpleMessageConverter;
    private final ProjectIssueModelConverter projectIssueModelConverter;
    private final IssueTrackerSearcher<String> issueTrackerSearcher;

    public JiraCloudModelExtractor(IssueTrackerMessageFormatter formatter, IssueTrackerSearcher<String> issueTrackerSearcher) {
        this.issueTrackerSimpleMessageConverter = new JiraCloudIssueTrackerSimpleMessageConverter(formatter);
        this.projectIssueModelConverter = new ProjectIssueModelConverter(formatter);
        this.issueTrackerSearcher = issueTrackerSearcher;
    }

    public final IssueTrackerModelHolder<String> extractSimpleMessageIssueModels(List<SimpleMessage> simpleMessages, String jobName) {
        List<IssueCreationModel> simpleMessageIssueCreationModels = new ArrayList<>(simpleMessages.size());
        for (SimpleMessage simpleMessage : simpleMessages) {
            IssueCreationModel simpleMessageIssueCreationModel = issueTrackerSimpleMessageConverter.convertToIssueCreationModel(simpleMessage, jobName);
            simpleMessageIssueCreationModels.add(simpleMessageIssueCreationModel);
        }

        return new IssueTrackerModelHolder<>(simpleMessageIssueCreationModels, List.of(), List.of());
    }

    public final IssueTrackerModelHolder<String> extractProjectMessageIssueModels(ProjectMessage projectMessage, String jobName) throws AlertException {
        IssueTrackerModelHolder<String> combinedResults = new IssueTrackerModelHolder<>(List.of(), List.of(), List.of());
        List<ActionableIssueSearchResult<String>> searchResults = issueTrackerSearcher.findIssues(projectMessage);
        for (ActionableIssueSearchResult<String> searchResult : searchResults) {
            IssueTrackerModelHolder<String> searchResultMessages = convertSearchResult(searchResult, jobName);
            combinedResults = IssueTrackerModelHolder.reduce(combinedResults, searchResultMessages);
        }
        return combinedResults;
    }

    private IssueTrackerModelHolder<String> convertSearchResult(ActionableIssueSearchResult<String> searchResult, String jobName) {
        Optional<ExistingIssueDetails<String>> existingIssueDetails = searchResult.getExistingIssueDetails();
        ProjectIssueModel projectIssueModel = searchResult.getProjectIssueModel();
        if (existingIssueDetails.isPresent()) {
            return convertExistingIssue(existingIssueDetails.get(), projectIssueModel, searchResult.getRequiredOperation());
        } else {
            IssueCreationModel issueCreationModel = projectIssueModelConverter.toIssueCreationModel(projectIssueModel, jobName, searchResult.getSearchQuery());
            return new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        }
    }

    private IssueTrackerModelHolder<String> convertExistingIssue(ExistingIssueDetails<String> existingIssueDetails, ProjectIssueModel projectIssueModel, ItemOperation requiredOperation) {
        List<IssueTransitionModel<String>> transitionModels = new ArrayList<>(1);
        List<IssueCommentModel<String>> commentModels = new ArrayList<>(1);
        if (ItemOperation.UPDATE.equals(requiredOperation) || ItemOperation.INFO.equals(requiredOperation)) {
            IssueCommentModel<String> projectIssueCommentModel = projectIssueModelConverter.toIssueCommentModel(existingIssueDetails, projectIssueModel);
            commentModels.add(projectIssueCommentModel);
        } else {
            IssueTransitionModel<String> projectIssueTransitionModel = projectIssueModelConverter.toIssueTransitionModel(existingIssueDetails, projectIssueModel, requiredOperation);
            transitionModels.add(projectIssueTransitionModel);
        }
        return new IssueTrackerModelHolder<>(List.of(), transitionModels, commentModels);
    }
}
