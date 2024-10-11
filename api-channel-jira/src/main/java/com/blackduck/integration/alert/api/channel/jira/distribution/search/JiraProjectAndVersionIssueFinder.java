/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.List;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueFinder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectVersionIssueFinder;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class JiraProjectAndVersionIssueFinder implements ProjectIssueFinder<String>, ProjectVersionIssueFinder<String> {
    private final String jiraProjectKey;
    private final JqlQueryExecutor jqlQueryExecutor;
    private final JiraIssueSearchResultCreator searchResultCreator;

    public JiraProjectAndVersionIssueFinder(String jiraProjectKey, JqlQueryExecutor jqlQueryExecutor, JiraIssueSearchResultCreator searchResultCreator) {
        this.jiraProjectKey = jiraProjectKey;
        this.jqlQueryExecutor = jqlQueryExecutor;
        this.searchResultCreator = searchResultCreator;
    }

    @Override
    public IssueTrackerSearchResult<String> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project);
        return findIssues(jqlString, providerDetails, project);
    }

    @Override
    public IssueTrackerSearchResult<String> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectVersionIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project, projectVersion);
        return findIssues(jqlString, providerDetails, project);
    }

    private IssueTrackerSearchResult<String> findIssues(String jqlString, ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        List<JiraSearcherResponseModel> issueResponseModels = jqlQueryExecutor.executeQuery(jqlString);
        List<ProjectIssueSearchResult<String>> searchResults = searchResultCreator.createResultsFromExistingIssues(providerDetails, project, issueResponseModels);
        return new IssueTrackerSearchResult<>(jqlString, searchResults);
    }

}
