package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;

public class JiraSearcherFactory {
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraIssueStatusCreator jiraIssueStatusCreator;
    private final JiraIssueTransitionRetriever jiraIssueTransitionRetriever;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final ProjectMessageToIssueModelTransformer modelTransformer;

    public JiraSearcherFactory(
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraIssueStatusCreator jiraIssueStatusCreator,
        JiraIssueTransitionRetriever jiraIssueTransitionRetriever,
        IssueCategoryRetriever issueCategoryRetriever,
        ProjectMessageToIssueModelTransformer modelTransformer
    ) {
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
            projectIssueFinder,
            projectIssueFinder,
            componentIssueFinder,
            exactIssueFinder,
            modelTransformer
        );
    }

}
