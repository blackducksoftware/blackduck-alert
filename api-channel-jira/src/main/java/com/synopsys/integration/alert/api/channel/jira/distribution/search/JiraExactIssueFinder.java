/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExactIssueFinder;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.IssueTrackerSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class JiraExactIssueFinder implements ExactIssueFinder<String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String jiraProjectKey;
    private final JqlQueryExecutor jqlQueryExecutor;
    private final JiraIssueSearchResultCreator searchResultCreator;
    private final IssueCategoryRetriever issueCategoryRetriever;

    public JiraExactIssueFinder(String jiraProjectKey, JqlQueryExecutor jqlQueryExecutor, JiraIssueSearchResultCreator searchResultCreator, IssueCategoryRetriever issueCategoryRetriever) {
        this.jiraProjectKey = jiraProjectKey;
        this.jqlQueryExecutor = jqlQueryExecutor;
        this.searchResultCreator = searchResultCreator;
        this.issueCategoryRetriever = issueCategoryRetriever;
    }

    @Override
    public IssueTrackerSearchResult<String> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();

        ComponentConcernType concernType = ComponentConcernType.VULNERABILITY;
        String policyName = null;

        Optional<IssuePolicyDetails> policyDetails = projectIssueModel.getPolicyDetails();
        Optional<String> optionalPolicyName = policyDetails.map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            concernType = ComponentConcernType.POLICY;
            policyName = optionalPolicyName.get();
        }

        if (projectIssueModel.getComponentUnknownVersionDetails().isPresent()) {
            concernType = ComponentConcernType.UNKNOWN_VERSION;
        }

        String jqlString = JqlStringCreator.createBlackDuckComponentConcernIssuesSearchString(
            jiraProjectKey,
            provider,
            project,
            projectIssueModel.getProjectVersion().orElse(null),
            bomComponent.getComponent(),
            bomComponent.getComponentVersion().orElse(null),
            concernType,
            policyName
        );
        logger.debug("Searching for Jira issues with this Query: {}", jqlString);
        List<ProjectIssueSearchResult<String>> searchResults = jqlQueryExecutor.executeQuery(jqlString)
            .stream()
            .map(jiraSearcherResponseModel -> searchResultCreator.createIssueResult(jiraSearcherResponseModel, projectIssueModel))
            .collect(Collectors.toList());
        return new IssueTrackerSearchResult<>(jqlString, searchResults);
    }

}
