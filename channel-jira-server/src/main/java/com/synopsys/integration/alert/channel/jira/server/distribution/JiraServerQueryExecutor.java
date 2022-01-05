/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JqlQueryExecutor;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;

public class JiraServerQueryExecutor implements JqlQueryExecutor {
    private final IssueSearchService issueSearchService;

    public JiraServerQueryExecutor(IssueSearchService issueSearchService) {
        this.issueSearchService = issueSearchService;
    }

    @Override
    public List<JiraSearcherResponseModel> executeQuery(String jql) throws AlertException {
        IssueSearchResponseModel issueSearchResponseModel = queryForIssues(jql);
        return issueSearchResponseModel.getIssues()
                   .stream()
                   .map(this::convertModel)
                   .collect(Collectors.toList());
    }

    private IssueSearchResponseModel queryForIssues(String jql) throws AlertException {
        try {
            return issueSearchService.queryForIssues(jql);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to query for Jira Server issues", e);
        }
    }

    private JiraSearcherResponseModel convertModel(IssueSearchIssueComponent issue) {
        String issueUrl = issue.getFields().getIssueType().getSelf();
        String summary = issue.getFields().getSummary();
        return new JiraSearcherResponseModel(issueUrl, issue.getKey(), issue.getId(), summary);
    }

}
