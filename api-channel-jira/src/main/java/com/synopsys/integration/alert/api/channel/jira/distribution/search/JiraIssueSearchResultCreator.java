/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.synopsys.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;

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

    public List<ProjectIssueSearchResult<String>> createResultsFromExistingIssues(ProviderDetails providerDetails, LinkableItem project, List<JiraSearcherResponseModel> issueResponseModels) throws AlertException {
        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            ProjectIssueSearchResult<String> resultFromExistingIssue = createResultFromProjectIssue(model, providerDetails, project);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    public ProjectIssueSearchResult<String> createResultFromProjectIssue(JiraSearcherResponseModel issue, ProviderDetails providerDetails, LinkableItem project) throws AlertException {
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
