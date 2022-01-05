/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExactIssueFinder;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectVersionComponentIssueFinder;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

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
    public List<ProjectIssueSearchResult<Integer>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails bomComponent) throws AlertException {
        AzureSearchFieldMappingBuilder fieldRefNameToValue = createBomFieldReferenceToValueMap(projectVersion, bomComponent);
        List<WorkItemResponseModel> workItems = workItemFinder.findWorkItems(providerDetails.getProvider(), project, fieldRefNameToValue);

        List<ProjectIssueSearchResult<Integer>> searchResults = new ArrayList<>(workItems.size());
        for (WorkItemResponseModel workItem : workItems) {
            IssueBomComponentDetails issueBomComponent = IssueBomComponentDetails.fromBomComponentDetails(bomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponent);
            ExistingIssueDetails<Integer> issueDetails = issueDetailsCreator.createIssueDetails(workItem, workItem.createFieldsWrapper(gson), projectIssueModel);

            ProjectIssueSearchResult<Integer> searchResult = new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
            searchResults.add(searchResult);
        }
        return searchResults;
    }

    @Override
    public List<ExistingIssueDetails<Integer>> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
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
        }

        if (projectIssueModel.getComponentUnknownVersionDetails().isPresent()) {
            categoryKey = AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_COMPONENT_UNKNOWN_VERSION_COMPATIBILITY_LABEL;
        }

        fieldRefNameToValue.addCategoryKey(categoryKey);

        return workItemFinder.findWorkItems(projectIssueModel.getProvider(), projectIssueModel.getProject(), fieldRefNameToValue)
            .stream()
            .map(workItemResponseModel -> createIssueDetails(workItemResponseModel, projectIssueModel))
            .collect(Collectors.toList());
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

    private ExistingIssueDetails<Integer> createIssueDetails(WorkItemResponseModel workItem, ProjectIssueModel projectIssueModel) {
        return issueDetailsCreator.createIssueDetails(workItem, workItem.createFieldsWrapper(gson), projectIssueModel);
    }

}
