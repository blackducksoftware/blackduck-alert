/*
 * channel-jira-cloud
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcher;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

public class JiraCloudSearcher extends JiraSearcher {
    private final IssueSearchService issueSearchService;

    public JiraCloudSearcher(String jiraProjectKey, IssueSearchService issueSearchService, JiraIssueAlertPropertiesManager issuePropertiesManager) {
        super(jiraProjectKey, issuePropertiesManager);
        this.issueSearchService = issueSearchService;
    }

    @Override
    protected List<JiraSearcherResponseModel> executeQueryForIssues(String jql) throws IntegrationException {
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssues(jql);
        return issueSearchResponseModel.getIssues()
                   .stream()
                   .map(this::convertModel)
                   .collect(Collectors.toList());
    }

    private JiraSearcherResponseModel convertModel(IssueResponseModel issue) {
        IssueFieldsComponent nullableIssueFields = issue.getFields();
        String existingIssueSummary = Optional.ofNullable(nullableIssueFields)
                                          .map(IssueFieldsComponent::getSummary)
                                          .orElse(issue.getKey());
        return new JiraSearcherResponseModel(issue.getSelf(), issue.getKey(), issue.getId(), existingIssueSummary);
    }

}
