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

import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.synopsys.integration.alert.api.channel.jira.util.JiraCallbackUtils;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.StatusCategory;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.components.TransitionComponent;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public abstract class JiraSearcher extends IssueTrackerSearcher<String> {
    private final String jiraProjectKey;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;
    private final JiraIssueStatusCreator jiraIssueStatusCreator;

    protected JiraSearcher(String jiraProjectKey, JiraIssueAlertPropertiesManager issuePropertiesManager, JiraIssueStatusCreator jiraIssueStatusCreator) {
        this.jiraProjectKey = jiraProjectKey;
        this.issuePropertiesManager = issuePropertiesManager;
        this.jiraIssueStatusCreator = jiraIssueStatusCreator;
    }

    protected abstract List<JiraSearcherResponseModel> executeQueryForIssues(String jql) throws IntegrationException;

    protected abstract StatusDetailsComponent fetchIssueStatus(String issueKey) throws IntegrationException;

    protected abstract TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;

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

        IssueCategory issueCategory = getIssueCategoryFromComponentConcernType(concernType);
        return queryForIssues(jqlString)
                   .stream()
                   .map(jiraSearcherResponseModel -> createExistingIssueDetails(jiraSearcherResponseModel, issueCategory))
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
        //TODO: This is similar to findExistingIssuesByProjectIssueModel.
        //  Is it always going to  be either a vulnerability or a policy? Could it happen on a BOM?
        ComponentConcernType concernType = ComponentConcernType.VULNERABILITY;
        if (projectIssueModel.getPolicyDetails().isPresent()) {
            concernType = ComponentConcernType.POLICY;
        }
        IssueCategory issueCategory = getIssueCategoryFromComponentConcernType(concernType);
        ExistingIssueDetails<String> issueDetails = createExistingIssueDetails(issue, issueCategory);
        return new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
    }

    private ExistingIssueDetails<String> createExistingIssueDetails(JiraSearcherResponseModel issue, IssueCategory issueCategory) {
        String issueCallbackLink = JiraCallbackUtils.createUILink(issue);
        IssueStatus issueStatus = jiraIssueStatusCreator.createIssueStatus(issue, this::fetchIssueStatus, this::fetchIssueTransitions);
        return new ExistingIssueDetails<>(issue.getIssueId(), issue.getIssueKey(), issue.getSummaryField(), issueCallbackLink, issueStatus, issueCategory);
    }

    private IssueCategory getIssueCategoryFromComponentConcernType(ComponentConcernType componentConcernType) {
        //TODO: If BOM doesn't get used, remove the default and make this an if/else statement
        IssueCategory issueCategory = IssueCategory.BOM;
        if (componentConcernType.equals(ComponentConcernType.VULNERABILITY)) {
            issueCategory = IssueCategory.VULNERABILITY;
        }
        if (componentConcernType.equals(ComponentConcernType.POLICY)) {
            issueCategory = IssueCategory.POLICY;
        }
        return issueCategory;
    }

    //TODO: If these methods are unused, they should be removed (Now exists in  JiraIssueStatusCreator)
    private StatusCategory retrieveIssueStatusCategory(String issueKey) throws AlertException {
        try {
            StatusDetailsComponent issueStatus = fetchIssueStatus(issueKey);
            return issueStatus.getStatusCategory();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve issue status from Jira. Issue Key: %s", issueKey), e);
        }
    }

    private List<TransitionComponent> retrieveTransitions(String issueKey) throws AlertException {
        try {
            TransitionsResponseModel transitionsResponse = fetchIssueTransitions(issueKey);
            return transitionsResponse.getTransitions();
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Failed to retrieve transitions from Jira. Issue Key: %s", issueKey), e);
        }
    }

}
