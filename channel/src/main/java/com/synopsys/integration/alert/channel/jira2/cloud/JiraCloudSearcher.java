/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.jira2.cloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira2.common.JqlStringCreator;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopys.integration.alert.channel.api.issue.IssueTrackerSearcher;
import com.synopys.integration.alert.channel.api.issue.model.ActionableIssueSearchResult;
import com.synopys.integration.alert.channel.api.issue.model.ExistingIssueDetails;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopys.integration.alert.channel.api.issue.model.ProjectIssueSearchResult;

public class JiraCloudSearcher extends IssueTrackerSearcher<String> {
    private final String jiraProjectKey;
    private final IssueSearchService issueSearchService;
    private final JiraIssueAlertPropertiesManager issuePropertiesManager;

    public JiraCloudSearcher(String jiraProjectKey, IssueSearchService issueSearchService, JiraIssueAlertPropertiesManager issuePropertiesManager) {
        this.jiraProjectKey = jiraProjectKey;
        this.issueSearchService = issueSearchService;
        this.issuePropertiesManager = issuePropertiesManager;
    }

    @Override
    protected List<ProjectIssueSearchResult<String>> findProjectIssues(LinkableItem provider, LinkableItem project) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectIssuesSearchString(jiraProjectKey, provider, project);
        return findIssues(jqlString, provider, project);
    }

    @Override
    protected List<ProjectIssueSearchResult<String>> findProjectVersionIssues(LinkableItem provider, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        String jqlString = JqlStringCreator.createBlackDuckProjectVersionIssuesSearchString(jiraProjectKey, provider, project, projectVersion);
        return findIssues(jqlString, provider, project);
    }

    @Override
    protected List<ProjectIssueSearchResult<String>> findIssuesByComponent(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, BomComponentDetails originalBomComponent) throws AlertException {
        LinkableItem component = originalBomComponent.getComponent();
        LinkableItem nullableComponentVersion = originalBomComponent.getComponentVersion().orElse(null);

        String jqlString = JqlStringCreator.createBlackDuckComponentIssuesSearchString(jiraProjectKey, provider, project, projectVersion, component, nullableComponentVersion);
        List<IssueResponseModel> issueResponseModels = queryForIssues(jqlString);

        BomComponentDetails relevantDetails = new BomComponentDetails(
            originalBomComponent.getComponent(),
            originalBomComponent.getComponentVersion().orElse(null),
            List.of(),
            originalBomComponent.getLicense(),
            originalBomComponent.getUsage(),
            originalBomComponent.getAdditionalAttributes(),
            originalBomComponent.getBlackDuckIssuesUrl()
        );

        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (IssueResponseModel model : issueResponseModels) {
            ProjectIssueSearchResult<String> resultFromExistingIssue = createIssueResult(model, provider, project, projectVersion, relevantDetails);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    @Override
    protected ActionableIssueSearchResult<String> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        LinkableItem provider = projectIssueModel.getProvider();
        LinkableItem project = projectIssueModel.getProject();
        BomComponentDetails bomComponent = projectIssueModel.getBomComponent();

        List<ComponentConcern> componentConcerns = bomComponent.getComponentConcerns();
        ComponentConcern arbitraryComponentConcern = componentConcerns
                                                         .stream()
                                                         .findAny()
                                                         .orElseThrow(() -> new AlertRuntimeException("Unable to search for issue. Missing required component concern"));

        String jqlString = JqlStringCreator.createBlackDuckComponentConcernIssuesSearchString(
            jiraProjectKey,
            provider,
            project,
            projectIssueModel.getProjectVersion().orElse(null),
            bomComponent.getComponent(),
            bomComponent.getComponentVersion().orElse(null),
            arbitraryComponentConcern
        );

        List<IssueResponseModel> issueResponseModels = queryForIssues(jqlString);
        int foundIssuesCount = issueResponseModels.size();

        ExistingIssueDetails<String> existingIssueDetails = null;
        ItemOperation operation;

        if (foundIssuesCount == 1) {
            IssueResponseModel issue = issueResponseModels.get(0);
            existingIssueDetails = createExistingIssueDetails(issue);

            operation = ItemOperation.UPDATE;

            // TODO we might need more granularity in policy / vulnerability concerns at the bom component level
            boolean onlyPolicyDeletes = componentConcerns
                                            .stream()
                                            .filter(concern -> ComponentConcernType.POLICY.equals(concern.getType()))
                                            .map(ComponentConcern::getOperation)
                                            .allMatch(ItemOperation.DELETE::equals);
            if (onlyPolicyDeletes) {
                operation = ItemOperation.DELETE;
            }
        } else if (foundIssuesCount > 1) {
            throw new AlertException("Expect to find a unique issue, but more than one issue was found");
        } else {
            operation = ItemOperation.ADD;
        }

        return new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, operation);
    }

    private List<ProjectIssueSearchResult<String>> findIssues(String jqlString, LinkableItem provider, LinkableItem project) throws AlertException {
        List<IssueResponseModel> issueResponseModels = queryForIssues(jqlString);
        return createResultsFromExistingIssues(provider, project, issueResponseModels);
    }

    private List<IssueResponseModel> queryForIssues(String jql) throws AlertException {
        try {
            IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssues(jql);
            return issueSearchResponseModel.getIssues();
        } catch (IntegrationException e) {
            throw new AlertException("Failed to query for Jira issues", e);
        }
    }

    private List<ProjectIssueSearchResult<String>> createResultsFromExistingIssues(LinkableItem provider, LinkableItem project, List<IssueResponseModel> issueResponseModels) throws AlertException {
        List<ProjectIssueSearchResult<String>> searchResults = new ArrayList<>();
        for (IssueResponseModel model : issueResponseModels) {
            ProjectIssueSearchResult<String> resultFromExistingIssue = createResultFromProjectIssue(model, provider, project);
            searchResults.add(resultFromExistingIssue);
        }
        return searchResults;
    }

    private ProjectIssueSearchResult<String> createResultFromProjectIssue(IssueResponseModel issue, LinkableItem provider, LinkableItem project) throws AlertException {
        JiraIssueSearchProperties issueProperties = issuePropertiesManager.retrieveIssueProperties(issue.getKey());

        String nullableSubComponentName = issueProperties.getSubComponentName();
        String nullableSubComponentValue = issueProperties.getSubComponentValue();
        LinkableItem componentVersion = null;
        if (StringUtils.isNotBlank(nullableSubComponentName) && StringUtils.isNotBlank(nullableSubComponentValue)) {
            componentVersion = new LinkableItem(nullableSubComponentName, nullableSubComponentValue);
        }

        LinkableItem projectVersion = new LinkableItem(issueProperties.getSubTopicName(), issueProperties.getSubTopicValue());
        BomComponentDetails bomComponent = createMinimalBomComponentDetails(
            new LinkableItem(issueProperties.getComponentName(), issueProperties.getComponentValue()),
            componentVersion
        );
        return createIssueResult(issue, provider, project, projectVersion, bomComponent);
    }

    private ProjectIssueSearchResult<String> createIssueResult(
        IssueResponseModel issue,
        LinkableItem provider,
        LinkableItem project,
        LinkableItem projectVersion,
        BomComponentDetails relevantDetails
    ) {
        ProjectIssueModel projectIssueModel = new ProjectIssueModel(provider, project, projectVersion, relevantDetails);
        ExistingIssueDetails<String> issueDetails = createExistingIssueDetails(issue);
        return new ProjectIssueSearchResult<>(issue.getId(), issueDetails, projectIssueModel);
    }

    private BomComponentDetails createMinimalBomComponentDetails(LinkableItem component, @Nullable LinkableItem componentVersion) {
        return new BomComponentDetails(
            component,
            componentVersion,
            List.of(),
            new LinkableItem("License", "Unknown License"),
            "Unknown Usage",
            List.of(),
            ""
        );
    }

    private ExistingIssueDetails<String> createExistingIssueDetails(IssueResponseModel issue) {
        String issueCallbackLink = JiraCallbackUtils.createUILink(issue);
        IssueFieldsComponent issueFields = issue.getFields();
        return new ExistingIssueDetails<>(issue.getId(), issue.getKey(), issueFields.getSummary(), issueCallbackLink);
    }

}
