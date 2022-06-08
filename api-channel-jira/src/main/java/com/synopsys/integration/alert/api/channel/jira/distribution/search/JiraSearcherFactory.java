/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import com.synopsys.integration.alert.api.channel.issue.IssueTrackerChannelLock;
import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.IssueTrackerSearcher;

public class JiraSearcherFactory {
    private final IssueTrackerChannelLock channelLock;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraIssueStatusCreator jiraIssueStatusCreator;
    private final JiraIssueTransitionRetriever jiraIssueTransitionRetriever;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final ProjectMessageToIssueModelTransformer modelTransformer;

    public JiraSearcherFactory(
        IssueTrackerChannelLock channelLock,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraIssueStatusCreator jiraIssueStatusCreator,
        JiraIssueTransitionRetriever jiraIssueTransitionRetriever,
        IssueCategoryRetriever issueCategoryRetriever,
        ProjectMessageToIssueModelTransformer modelTransformer
    ) {
        this.channelLock = channelLock;
        this.issuePropertiesManager = issuePropertiesManager;
        this.jiraIssueStatusCreator = jiraIssueStatusCreator;
        this.jiraIssueTransitionRetriever = jiraIssueTransitionRetriever;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.modelTransformer = modelTransformer;
    }

    public IssueTrackerSearcher<String> createJiraSearcher(String jiraProjectKey, JqlQueryExecutor jqlQueryExecutor) {
        JiraIssueSearchResultCreator searchResultCreator = new JiraIssueSearchResultCreator(issuePropertiesManager, jiraIssueStatusCreator, jiraIssueTransitionRetriever, issueCategoryRetriever);
        JiraProjectAndVersionIssueFinder projectIssueFinder = new JiraProjectAndVersionIssueFinder(jiraProjectKey, jqlQueryExecutor, searchResultCreator);
        JiraComponentIssueFinder componentIssueFinder = new JiraComponentIssueFinder(jiraProjectKey, jqlQueryExecutor, searchResultCreator);
        JiraExactIssueFinder exactIssueFinder = new JiraExactIssueFinder(jiraProjectKey, jqlQueryExecutor, searchResultCreator, issueCategoryRetriever);

        return new IssueTrackerSearcher<>(
            channelLock,
            projectIssueFinder,
            projectIssueFinder,
            componentIssueFinder,
            exactIssueFinder,
            modelTransformer
        );
    }

}
