/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;

public class JiraIssueSearchResultCreator {
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraIssueStatusCreator jiraIssueStatusCreator;
    private final JiraIssueTransitionRetriever jiraIssueTransitionRetriever;
    private final IssueCategoryRetriever issueCategoryRetriever;

    public JiraIssueSearchResultCreator(
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraIssueStatusCreator jiraIssueStatusCreator,
        JiraIssueTransitionRetriever jiraIssueTransitionRetriever,
        IssueCategoryRetriever issueCategoryRetriever
    ) {
        this.issuePropertiesManager = issuePropertiesManager;
        this.jiraIssueStatusCreator = jiraIssueStatusCreator;
        this.jiraIssueTransitionRetriever = jiraIssueTransitionRetriever;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    public List<ProjectIssueSearchResult<String>> createResultsFromExistingIssues(
        ProviderDetails providerDetails,
        LinkableItem project,
        List<JiraSearcherResponseModel> issueResponseModels
    ) throws AlertException {
        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            ProjectIssueSearchResult<String> resultFromExistingIssue = createResultFromProjectIssue(model, providerDetails, project);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    public ProjectIssueSearchResult<String> createResultFromProjectIssue(JiraSearcherResponseModel issue, ProviderDetails providerDetails, LinkableItem project)
        throws AlertException {
        JiraIssueSearchProperties issueProperties = issuePropertiesManager.retrieveIssueProperties(issue.getIssueKey());

        String nullableSubComponentName = issueProperties.getSubComponentName();
        String nullableSubComponentValue = issueProperties.getSubComponentValue();
        LinkableItem componentVersion = null;
        if (StringUtils.isNotBlank(nullableSubComponentName) && StringUtils.isNotBlank(nullableSubComponentValue)) {
            componentVersion = new LinkableItem(nullableSubComponentName, nullableSubComponentValue);
        }

        LinkableItem projectVersion = new LinkableItem(issueProperties.getSubTopicName(), issueProperties.getSubTopicValue());

        IssueBomComponentDetails bomComponentDetails = IssueBomComponentDetails.fromSearchResults(
            new LinkableItem(issueProperties.getComponentName(), issueProperties.getComponentValue()),
            componentVersion
        );

        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, bomComponentDetails);
        return createIssueResult(issue, projectIssueModel);
    }

    public ProjectIssueSearchResult<String> createIssueResult(JiraSearcherResponseModel issue, ProjectIssueModel projectIssueModel) {
        IssueCategory issueCategory = issueCategoryRetriever.retrieveIssueCategoryFromProjectIssueModel(projectIssueModel);
        ExistingIssueDetails<String> issueDetails = createExistingIssueDetails(issue, issueCategory);
        return new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
    }

    public ExistingIssueDetails<String> createExistingIssueDetails(JiraSearcherResponseModel issue, IssueCategory issueCategory) {
        String issueCallbackLink = JiraCallbackUtils.createUILink(issue);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(issue, jiraIssueTransitionRetriever::fetchIssueTransitions);
        return new ExistingIssueDetails<>(issue.getIssueId(), issue.getIssueKey(), issue.getSummaryField(), issueCallbackLink, issueStatus, issueCategory);
    }

}
