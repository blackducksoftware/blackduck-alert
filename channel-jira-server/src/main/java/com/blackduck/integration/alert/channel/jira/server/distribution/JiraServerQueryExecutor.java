package com.blackduck.integration.alert.channel.jira.server.distribution;

import java.util.List;
import java.util.stream.Collectors;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JqlQueryExecutor;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;

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
