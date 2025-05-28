/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExactIssueFinder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueSearchResult;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

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
        return findExistingIssuesByProjectIssueModel(projectIssueModel, Integer.MAX_VALUE);
    }

    @Override
    public IssueTrackerSearchResult<String> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel, Integer maxResults) throws AlertException {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();
        LinkableItem componentVersion = bomComponent.getComponentVersion().orElse(null);

        ComponentConcernType concernType = ComponentConcernType.VULNERABILITY;
        String policyName = null;

        Optional<IssuePolicyDetails> policyDetails = projectIssueModel.getPolicyDetails();
        Optional<String> optionalPolicyName = policyDetails.map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            concernType = ComponentConcernType.POLICY;
            policyName = optionalPolicyName.get();

            if(policyDetails.map(IssuePolicyDetails::getOperation).filter(ItemOperation.DELETE::equals).isPresent()) {
                // policy cleared notification or override.  The component version doesn't matter just component
                // and policy name.
                componentVersion = null;
            }
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
            componentVersion,
            concernType,
            policyName
        );
        logger.debug("Searching for Jira issues with this Query: {}", jqlString);
        List<ProjectIssueSearchResult<String>> searchResults;
        if(maxResults == Integer.MAX_VALUE) {
            searchResults = jqlQueryExecutor.executeQuery(jqlString)
                    .stream()
                    .map(jiraSearcherResponseModel -> searchResultCreator.createIssueResult(jiraSearcherResponseModel, projectIssueModel))
                    .toList();
        } else {
            searchResults = jqlQueryExecutor.executeQuery(jqlString, maxResults)
                    .stream()
                    .map(jiraSearcherResponseModel -> searchResultCreator.createIssueResult(jiraSearcherResponseModel, projectIssueModel))
                    .toList();
        }
        return new IssueTrackerSearchResult<>(jqlString, searchResults);
    }

}
