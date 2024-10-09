package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import java.util.ArrayList;
import java.util.List;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectVersionComponentIssueFinder;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class JiraComponentIssueFinder implements ProjectVersionComponentIssueFinder<String> {
    private final String jiraProjectKey;
    private final JqlQueryExecutor jqlQueryExecutor;
    private final JiraIssueSearchResultCreator searchResultCreator;

    public JiraComponentIssueFinder(String jiraProjectKey, JqlQueryExecutor jqlQueryExecutor, JiraIssueSearchResultCreator searchResultCreator) {
        this.jiraProjectKey = jiraProjectKey;
        this.jqlQueryExecutor = jqlQueryExecutor;
        this.searchResultCreator = searchResultCreator;
    }

    @Override
    public IssueTrackerSearchResult<String> findIssuesByComponent(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        BomComponentDetails originalBomComponent
    ) throws AlertException {
        LinkableItem component = originalBomComponent.getComponent();
        LinkableItem nullableComponentVersion = originalBomComponent.getComponentVersion().orElse(null);

        String jqlString = JqlStringCreator.createBlackDuckComponentIssuesSearchString(
            jiraProjectKey,
            providerDetails.getProvider(),
            project,
            projectVersion,
            component,
            nullableComponentVersion
        );
        List<JiraSearcherResponseModel> issueResponseModels = jqlQueryExecutor.executeQuery(jqlString);

        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(originalBomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponentDetails);
            ProjectIssueSearchResult<String> resultFromExistingIssue = searchResultCreator.createIssueResult(model, projectIssueModel);
            searchResults.add(resultFromExistingIssue);
        }
        return new IssueTrackerSearchResult<>(jqlString, searchResults);
    }

}
