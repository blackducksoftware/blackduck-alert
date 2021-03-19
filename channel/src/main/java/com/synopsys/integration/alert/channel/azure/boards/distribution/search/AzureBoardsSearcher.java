/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.channel.api.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ActionableIssueSearchResult;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.channel.api.issue.search.ProjectIssueSearchResult;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsUILinkUtils;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsWorkItemExtractionUtils;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhere;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class AzureBoardsSearcher extends IssueTrackerSearcher<Integer> {
    private final Gson gson;
    private final String organizationName;
    private final AzureBoardsIssueTrackerQueryManager queryManager;

    public AzureBoardsSearcher(Gson gson, String organizationName, AzureBoardsIssueTrackerQueryManager queryManager) {
        this.gson = gson;
        this.organizationName = organizationName;
        this.queryManager = queryManager;
    }

    @Override
    protected List<ProjectIssueSearchResult<Integer>> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException {
        return findWorkItemsAndConvertToSearchResults(providerDetails, project, null, Map.of());
    }

    @Override
    protected List<ProjectIssueSearchResult<Integer>> findProjectVersionIssues(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion) throws AlertException {
        String projectVersionItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(projectVersion);
        return findWorkItemsAndConvertToSearchResults(providerDetails, project, projectVersion, Map.of(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, projectVersionItemKey));
    }

    @Override
    protected List<ProjectIssueSearchResult<Integer>> findIssuesByComponent(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, BomComponentDetails bomComponent) throws AlertException {
        Map<String, String> fieldRefNameToValue = createBomFieldReferenceToValueMap(projectVersion, bomComponent);
        List<WorkItemResponseModel> workItems = findWorkItems(providerDetails.getProvider(), project, fieldRefNameToValue);

        List<ProjectIssueSearchResult<Integer>> searchResults = new ArrayList<>(workItems.size());
        for (WorkItemResponseModel workItem : workItems) {
            ExistingIssueDetails<Integer> issueDetails = createIssueDetails(workItem, workItem.createFieldsWrapper(gson));

            IssueBomComponentDetails issueBomComponent = IssueBomComponentDetails.fromBomComponentDetails(bomComponent);
            ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, issueBomComponent);

            ProjectIssueSearchResult<Integer> searchResult = new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
            searchResults.add(searchResult);
        }
        return searchResults;
    }

    @Override
    protected ActionableIssueSearchResult<Integer> findIssueByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException {
        LinkableItem projectVersion = projectIssueModel.getProjectVersion()
                                          .orElseThrow(() -> new AlertRuntimeException("Missing project-version"));
        IssueBomComponentDetails bomComponent = projectIssueModel.getBomComponentDetails();

        Map<String, String> fieldRefNameToValue = createBomFieldReferenceToValueMap(projectVersion, bomComponent);

        String categoryKey = AzureBoardsAlertIssuePropertiesManager.CATEGORY_TYPE_VULNERABILITY_COMPATIBILITY_LABEL;

        Optional<IssuePolicyDetails> policyDetails = projectIssueModel.getPolicyDetails();
        Optional<String> optionalPolicyName = policyDetails.map(IssuePolicyDetails::getName);
        if (optionalPolicyName.isPresent()) {
            categoryKey = AzureBoardsAlertIssuePropertiesManager.POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL;
            String additionalInfoKey = AzureBoardsAlertIssuePropertiesManager.POLICY_ADDITIONAL_KEY_COMPATIBILITY_LABEL + optionalPolicyName.get();
            fieldRefNameToValue.put(AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey);
        }

        fieldRefNameToValue.put(AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey);

        List<WorkItemResponseModel> workItems = findWorkItems(projectIssueModel.getProvider(), projectIssueModel.getProject(), fieldRefNameToValue);

        ExistingIssueDetails<Integer> existingIssueDetails = null;
        ItemOperation searchResultOperation = ItemOperation.UPDATE;

        int foundIssuesCount = workItems.size();
        if (foundIssuesCount == 1) {
            WorkItemResponseModel workItem = workItems.get(0);
            WorkItemFieldsWrapper workItemFields = workItem.createFieldsWrapper(gson);
            existingIssueDetails = createIssueDetails(workItem, workItemFields);

            Optional<ItemOperation> policyOperation = policyDetails.map(IssuePolicyDetails::getOperation);
            if (policyOperation.isPresent()) {
                searchResultOperation = policyOperation.get();
            }
        } else if (foundIssuesCount > 1) {
            throw new AlertException("Expect to find a unique work item, but more than one work item was found");
        } else {
            searchResultOperation = ItemOperation.ADD;
        }

        return new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, searchResultOperation);
    }

    private Map<String, String> createBomFieldReferenceToValueMap(LinkableItem projectVersion, AbstractBomComponentDetails bomComponent) {
        String projectVersionItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(projectVersion);
        String componentItemKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(bomComponent.getComponent());
        Optional<String> optionalComponentVersionItemKey = bomComponent.getComponentVersion().map(AzureBoardsSearchPropertiesUtils::createNullableLinkableItemKey);

        Map<String, String> fieldRefNameToValue = new HashMap<>();
        fieldRefNameToValue.put(AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, projectVersionItemKey);
        fieldRefNameToValue.put(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentItemKey);
        optionalComponentVersionItemKey.ifPresent(componentVersionItemKey -> fieldRefNameToValue.put(AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentVersionItemKey));
        return fieldRefNameToValue;
    }

    private List<ProjectIssueSearchResult<Integer>> findWorkItemsAndConvertToSearchResults(
        ProviderDetails providerDetails,
        LinkableItem project,
        @Nullable LinkableItem projectVersion,
        Map<String, String> fieldReferenceNameToExpectedValue
    ) throws AlertException {
        List<WorkItemResponseModel> workItems = findWorkItems(providerDetails.getProvider(), project, fieldReferenceNameToExpectedValue);

        List<ProjectIssueSearchResult<Integer>> searchResults = new ArrayList<>(workItems.size());
        for (WorkItemResponseModel workItem : workItems) {
            WorkItemFieldsWrapper workItemFields = workItem.createFieldsWrapper(gson);
            ExistingIssueDetails<Integer> issueDetails = createIssueDetails(workItem, workItemFields);
            ProjectIssueModel projectIssueModel = createProjectIssueModel(providerDetails, project, projectVersion, workItemFields);
            ProjectIssueSearchResult<Integer> searchResult = new ProjectIssueSearchResult<>(issueDetails, projectIssueModel);
            searchResults.add(searchResult);
        }
        return searchResults;
    }

    private List<WorkItemResponseModel> findWorkItems(LinkableItem provider, LinkableItem project, Map<String, String> fieldReferenceNameToExpectedValue) throws AlertException {
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(project);

        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        WorkItemQueryWhere queryBuilder = WorkItemQuery
                                              .select(systemIdFieldName)
                                              .fromWorkItems()
                                              .whereGroup(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
                                              .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey);

        for (Map.Entry<String, String> refToValue : fieldReferenceNameToExpectedValue.entrySet()) {
            queryBuilder = queryBuilder.and(refToValue.getKey(), WorkItemQueryWhereOperator.EQ, refToValue.getValue());
        }

        WorkItemQuery query = queryBuilder.orderBy(systemIdFieldName).build();
        return queryManager.executeQueryAndRetrieveWorkItems(query);
    }

    private ExistingIssueDetails<Integer> createIssueDetails(WorkItemResponseModel workItem, WorkItemFieldsWrapper workItemFields) {
        Integer workItemId = workItem.getId();
        String workItemTitle = workItemFields.getField(WorkItemResponseFields.System_Title).orElse("Unknown Title");
        String workItemUILink = AzureBoardsUILinkUtils.extractUILink(organizationName, workItem);
        return new ExistingIssueDetails<>(workItemId, Objects.toString(workItemId), workItemTitle, workItemUILink);
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

        IssueBomComponentDetails bomComponent = new IssueBomComponentDetails(
            component,
            componentVersion,
            new LinkableItem("License", "Unknown License"),
            "Unknown Usage",
            List.of(),
            ""
        );
        return ProjectIssueModel.bom(providerDetails, project, projectVersion, bomComponent);
    }

}
