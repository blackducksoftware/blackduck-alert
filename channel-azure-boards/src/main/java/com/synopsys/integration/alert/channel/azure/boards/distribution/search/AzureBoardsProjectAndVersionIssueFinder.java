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

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueFinder;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.ProjectVersionIssueFinder;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsWorkItemExtractionUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class AzureBoardsProjectAndVersionIssueFinder implements ProjectIssueFinder<Integer>, ProjectVersionIssueFinder<Integer> {
    private final Gson gson;
    private final AzureBoardsExistingIssueDetailsCreator issueDetailsCreator;
    private final AzureBoardsWorkItemFinder workItemFinder;

    public AzureBoardsProjectAndVersionIssueFinder(Gson gson, AzureBoardsExistingIssueDetailsCreator issueDetailsCreator, AzureBoardsWorkItemFinder workItemFinder) {
        this.gson = gson;
        this.issueDetailsCreator = issueDetailsCreator;
        this.workItemFinder = workItemFinder;
    }

    @Override
    public List<ProjectIssueSearchResult<Integer>> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        return findWorkItemsAndConvertToSearchResults(providerDetails, project, null, AzureSearchFieldMappingBuilder.create());
    }

    @Override
    public List<ProjectIssueSearchResult<Integer>> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        String projectVersionItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(projectVersion);
        AzureSearchFieldMappingBuilder azureSearchFieldMappingBuilder = AzureSearchFieldMappingBuilder.create();
        azureSearchFieldMappingBuilder.addSubTopic(projectVersionItemKey);
        return findWorkItemsAndConvertToSearchResults(providerDetails, project, projectVersion, azureSearchFieldMappingBuilder);
    }

    private List<ProjectIssueSearchResult<Integer>> findWorkItemsAndConvertToSearchResults(
        ProviderDetails providerDetails,
        LinkableItem project,
        @Nullable LinkableItem projectVersion,
        AzureSearchFieldMappingBuilder fieldReferenceNameToExpectedValue
    ) throws AlertException {
        List<WorkItemResponseModel> workItems = workItemFinder.findWorkItems(providerDetails.getProvider(), project, fieldReferenceNameToExpectedValue);

        List<ProjectIssueSearchResult<Integer>> searchResults = new ArrayList<>(workItems.size());
        for (WorkItemResponseModel workItem : workItems) {
            WorkItemFieldsWrapper workItemFields = workItem.createFieldsWrapper(gson);
            ProjectIssueModel projectIssueModel = createProjectIssueModel(providerDetails, project, projectVersion, workItemFields);
            ExistingIssueDetails<Integer> issueDetails = issueDetailsCreator.createIssueDetails(workItem, workItemFields, projectIssueModel);
            ProjectIssueSearchResult<Integer> searchResult = new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
            searchResults.add(searchResult);
        }
        return searchResults;
    }

    private ProjectIssueModel createProjectIssueModel(ProviderDetails providerDetails, LinkableItem project, @Nullable LinkableItem nullableProjectVersion, WorkItemFieldsWrapper workItemFields) {
        AzureFieldDefinition<String> projectVersionFieldDef = AzureFieldDefinition.stringField(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME);
        AzureFieldDefinition<String> componentFieldDef = AzureFieldDefinition.stringField(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME);
        AzureFieldDefinition<String> subComponentFieldDef = AzureFieldDefinition.stringField(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME);

        LinkableItem projectVersion = Optional.ofNullable(nullableProjectVersion)
            .orElse(AzureBoardsWorkItemExtractionUtils.extractLinkableItem(workItemFields, projectVersionFieldDef));
        LinkableItem component = AzureBoardsWorkItemExtractionUtils.extractLinkableItem(workItemFields, componentFieldDef);

        LinkableItem componentVersion = null;
        Optional<String> componentVersionField = workItemFields.getField(subComponentFieldDef);
        if (componentVersionField.isPresent()) {
            componentVersion = AzureBoardsWorkItemExtractionUtils.extractLinkableItem(componentVersionField.get());
        }

        IssueBomComponentDetails bomComponent = IssueBomComponentDetails.fromSearchResults(component, componentVersion);
        return ProjectIssueModel.bom(providerDetails, project, projectVersion, bomComponent);
    }

}
