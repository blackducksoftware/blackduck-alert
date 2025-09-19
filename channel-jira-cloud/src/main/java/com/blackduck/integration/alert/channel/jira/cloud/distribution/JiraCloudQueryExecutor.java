/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JqlQueryExecutor;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;

public class JiraCloudQueryExecutor implements JqlQueryExecutor {
    private final IssueSearchService issueSearchService;

    public JiraCloudQueryExecutor(IssueSearchService issueSearchService) {
        this.issueSearchService = issueSearchService;
    }

    @Override
    public List<JiraSearcherResponseModel> executeQuery(String jql) throws AlertException {
        IssueSearchResponseModel issueSearchResponseModel = queryForIssues(jql);
        return issueSearchResponseModel.getIssues()
                   .stream()
                   .map(this::convertModel)
                   .toList();
    }

    @Override
    public List<JiraSearcherResponseModel> executeQuery(String jql, Integer maxResults) throws AlertException {
        IssueSearchResponseModel issueSearchResponseModel = queryForIssues(jql, maxResults);
        return issueSearchResponseModel.getIssues()
                .stream()
                .map(this::convertModel)
                .toList();
    }

    private IssueSearchResponseModel queryForIssues(String jql) throws AlertException {
        try {
            return issueSearchService.queryForIssues(jql);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to query for Jira Cloud issues", e);
        }
    }

    private IssueSearchResponseModel queryForIssues(String jql, Integer maxResults) throws AlertException {
        try {
            return issueSearchService.queryForIssuePage(jql, null, maxResults);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to query for Jira Cloud issues", e);
        }
    }

    private JiraSearcherResponseModel convertModel(IssueResponseModel issue) {
        IssueFieldsComponent nullableIssueFields = issue.getFields();
        String existingIssueSummary = Optional.ofNullable(nullableIssueFields)
                                          .map(IssueFieldsComponent::getSummary)
                                          .orElse(issue.getKey());
        return new JiraSearcherResponseModel(issue.getSelf(), issue.getKey(), issue.getId(), existingIssueSummary);
    }

}
