/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExactIssueFinder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ProjectVersionComponentIssueFinder;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.google.gson.Gson;

public class AzureBoardsComponentIssueFinder implements ProjectVersionComponentIssueFinder<Integer>, ExactIssueFinder<Integer> {
    private final Gson gson;
    private final AzureBoardsWorkItemFinder workItemFinder;
    private final AzureBoardsExistingIssueDetailsCreator issueDetailsCreator;

    public AzureBoardsComponentIssueFinder(Gson gson, AzureBoardsWorkItemFinder workItemFinder, AzureBoardsExistingIssueDetailsCreator issueDetailsCreator) {
        this.gson = gson;
        this.workItemFinder = workItemFinder;
        this.issueDetailsCreator = issueDetailsCreator;
    }

    @Override
    public IssueTrackerSearchResult<Integer> findIssuesByComponent(
        ProviderDetails providerDetails,
        LinkableItem project,
        LinkableItem projectVersion,
        BomComponentDetails bomComponent
    ) throws AlertException {
        AzureSearchFieldMappingBuilder fieldRefNameToValue = createBomFieldReferenceToValueMap(projectVersion, bomComponent);
        AzureBoardsWorkItemSearchResult result = workItemFinder.findWorkItems(providerDetails.getProvider(), project, fieldRefNameToValue);
        List<WorkItemResponseModel> workItems = result.getSearchResults();

        List<ProjectIssueSearchResult<Integer>> searchResults = new ArrayList<>(workItems.size());
        for (WorkItemResponseModel workItem : workItems) {
            IssueBomComponentDetails issueBomComponent = IssueBomComponentDetails.fromBomComponentDetails(bomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponent);
            ExistingIssueDetails<Integer> issueDetails = issueDetailsCreator.createIssueDetails(workItem, workItem.createFieldsWrapper(gson), projectIssueModel);

            ProjectIssueSearchResult<Integer> searchResult = new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
            searchResults.add(searchResult);
        }
        return new IssueTrackerSearchResult<>(result.getQuery().rawQuery(), searchResults);
    }

    @Override
    public IssueTrackerSearchResult<Integer> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel, Integer maxResults) throws AlertException {
        return findExistingIssuesByProjectIssueModel(projectIssueModel);
    }

    @Override
    public IssueTrackerSearchResult<Integer> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        LinkableItem projectVersion = projectIssueModel.getProjectVersion()
            .orElseThrow(() -> new AlertRuntimeException("Missing project-version"));

        String categoryKey = AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL;
        AzureSearchFieldMappingBuilder fieldRefNameToValue = createBomFieldReferenceToValueMap(projectVersion, projectIssueModel.getBomComponentDetails());

        Optional<IssuePolicyDetails> policyDetails = projectIssueModel.getPolicyDetails();
        Optional<String> optionalPolicyName = policyDetails.map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            categoryKey = AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_POLICY_COMPATIBILITY_LABEL;

            String additionalInfoKey = AzureBoardsAlertIssuePropertiesManager.POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + optionalPolicyName.get();
            fieldRefNameToValue.addAdditionalInfoKey(additionalInfoKey);

            if(policyDetails.map(IssuePolicyDetails::getOperation).filter(ItemOperation.DELETE::equals).isPresent()) {
                // policy cleared notification or override.  The component version doesn't matter just component
                // and policy name.
                fieldRefNameToValue.removeSubComponentKey();
            }
        }

        if (projectIssueModel.getComponentUnknownVersionDetails().isPresent()) {
            categoryKey = AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_COMPONENT_UNKNOWN_VERSION_COMPATIBILITY_LABEL;
        }

        fieldRefNameToValue.addCategoryKey(categoryKey);

        AzureBoardsWorkItemSearchResult result = workItemFinder.findWorkItems(projectIssueModel.getProvider(), projectIssueModel.getProject(), fieldRefNameToValue);
        List<ProjectIssueSearchResult<Integer>> searchResults = result.getSearchResults()
            .stream()
            .map(workItemResponseModel -> createIssueDetails(workItemResponseModel, projectIssueModel))
            .collect(Collectors.toList());
        return new IssueTrackerSearchResult<>(result.getQuery().rawQuery(), searchResults);
    }

    private AzureSearchFieldMappingBuilder createBomFieldReferenceToValueMap(LinkableItem projectVersion, AbstractBomComponentDetails bomComponent) {
        String projectVersionItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(projectVersion);
        String componentItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponent.getComponent());
        Optional<String> optionalComponentVersionItemKey = bomComponent.getComponentVersion().map(AzureBoardsSearchPropertiesUtils::createNullableLinkableItemKey);

        AzureSearchFieldMappingBuilder azureSearchFieldMappingBuilder = AzureSearchFieldMappingBuilder.create()
            .addSubTopic(projectVersionItemKey)
            .addComponentKey(componentItemKey);
        optionalComponentVersionItemKey.ifPresent(azureSearchFieldMappingBuilder::addSubComponentKey);
        return azureSearchFieldMappingBuilder;
    }

    private ProjectIssueSearchResult<Integer> createIssueDetails(WorkItemResponseModel workItem, ProjectIssueModel projectIssueModel) {
        return new ProjectIssueSearchResult<>(issueDetailsCreator.createIssueDetails(workItem, workItem.createFieldsWrapper(gson), projectIssueModel), projectIssueModel);
    }

}
