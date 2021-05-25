/*
 * api-channel-jira
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.synopsys.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.channel.api.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;

public abstract class JiraSearcher extends IssueTrackerSearcher<String> {
    private final String jiraProjectKey;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;

    protected JiraSearcher(String jiraProjectKey, JiraIssueAlertPropertiesManager issuePropertiesManager) {
        this.jiraProjectKey = jiraProjectKey;
        this.issuePropertiesManager = issuePropertiesManager;
    }

    protected abstract List<JiraSearcherResponseModel> executeQueryForIssues(String jql) throws IntegrationException;

    @Override
    protected final List<ProjectIssueSearchResult<String>> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project);
        return findIssues(jqlString, providerDetails, project);
    }

    @Override
    protected final List<ProjectIssueSearchResult<String>> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectVersionIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project, projectVersion);
        return findIssues(jqlString, providerDetails, project);
    }

    @Override
    protected final List<ProjectIssueSearchResult<String>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails originalBomComponent) throws AlertException {
        LinkableItem component = originalBomComponent.getComponent();
        LinkableItem nullableComponentVersion = originalBomComponent.getComponentVersion().orElse(null);

        String jqlString = JqlStringCreator.createBlackDuckComponentIssuesSearchString(jiraProjectKey, providerDetails.getProvider(), project, projectVersion, component, nullableComponentVersion);
        List<JiraSearcherResponseModel> issueResponseModels = queryForIssues(jqlString);

        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(originalBomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponentDetails);
            ProjectIssueSearchResult<String> resultFromExistingIssue = createIssueResult(model, projectIssueModel);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    @Override
    protected List<ExistingIssueDetails<String>> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
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

        return queryForIssues(jqlString)
                   .stream()
                   .map(this::createExistingIssueDetails)
                   .collect(Collectors.toList());
    }

    private List<ProjectIssueSearchResult<String>> findIssues(String jqlString, ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        List<JiraSearcherResponseModel> issueResponseModels = queryForIssues(jqlString);
        return createResultsFromExistingIssues(providerDetails, project, issueResponseModels);
    }

    private List<JiraSearcherResponseModel> queryForIssues(String jql) throws AlertException {
        try {
            return executeQueryForIssues(jql);
        } catch (IntegrationException e) {
            throw new AlertException("Failed to query for Jira issues", e);
        }
    }

    private List<ProjectIssueSearchResult<String>> createResultsFromExistingIssues(ProviderDetails providerDetails, LinkableItem project, List<JiraSearcherResponseModel> issueResponseModels) throws AlertException {
        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (JiraSearcherResponseModel model : issueResponseModels) {
            ProjectIssueSearchResult<String> resultFromExistingIssue = createResultFromProjectIssue(model, providerDetails, project);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    private ProjectIssueSearchResult<String> createResultFromProjectIssue(JiraSearcherResponseModel issue, ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        JiraIssueSearchProperties issueProperties = issuePropertiesManager.retrieveIssueProperties(issue.getIssueKey());

        String nullableSubComponentName = issueProperties.getSubComponentName();
        String nullableSubComponentValue = issueProperties.getSubComponentValue();
        LinkableItem componentVersion = null;
        if (StringUtils.isNotBlank(nullableSubComponentName) && StringUtils.isNotBlank(nullableSubComponentValue)) {
            componentVersion = new LinkableItem(nullableSubComponentName, nullableSubComponentValue);
        }

        LinkableItem projectVersion = new LinkableItem(issueProperties.getSubTopicName(), issueProperties.getSubTopicValue());

        IssueBomComponentDetails bomComponentDetails = IssueBomComponentDetails.fromSearchResults(
            new LinkableItem(issueProperties.getComponentName(), issueProperties.getComponentValue()),
            componentVersion
        );

        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, bomComponentDetails);
        return createIssueResult(issue, projectIssueModel);
    }

    private ProjectIssueSearchResult<String> createIssueResult(JiraSearcherResponseModel issue, ProjectIssueModel projectIssueModel) {
        ExistingIssueDetails<String> issueDetails = createExistingIssueDetails(issue);
        return new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
    }

    private ExistingIssueDetails<String> createExistingIssueDetails(JiraSearcherResponseModel issue) {
        String issueCallbackLink = JiraCallbackUtils.createUILink(issue);
        return new ExistingIssueDetails<>(issue.getIssueId(), issue.getIssueKey(), issue.getSummaryField(), issueCallbackLink);
    }

}
