/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectVersionComponentIssueFinder;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;

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
    public List<ProjectIssueSearchResult<String>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails originalBomComponent) throws AlertException {
        LinkableItem component = originalBomComponent.getComponent();
        LinkableItem nullableComponentVersion = originalBomComponent.getComponentVersion().orElse(null);

        String jqlString = JqlStringCreator.createBlackDuckComponentIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project, projectVersion, component, nullableComponentVersion);
        List<JiraSearcherResponseModel> issueResponseModels = jqlQueryExecutor.executeQuery(jqlString);

        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(originalBomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponentDetails);
            ProjectIssueSearchResult<String> resultFromExistingIssue = searchResultCreator.createIssueResult(model, projectIssueModel);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

}
