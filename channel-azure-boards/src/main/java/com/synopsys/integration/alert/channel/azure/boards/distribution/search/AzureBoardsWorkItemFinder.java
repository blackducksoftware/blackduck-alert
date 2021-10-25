/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.synopsys.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhere;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsWorkItemFinder {
    private final AzureBoardsIssueTrackerQueryManager queryManager;
    private final String teamProjectName;

    public AzureBoardsWorkItemFinder(AzureBoardsIssueTrackerQueryManager queryManager, String teamProjectName) {
        this.queryManager = queryManager;
        this.teamProjectName = teamProjectName;
    }

    public List<WorkItemResponseModel> findWorkItems(LinkableItem provider, LinkableItem project, AzureSearchFieldMappingBuilder fieldReferenceNameToExpectedValue) throws AlertException {
        WorkItemQueryWhere queryBuilder;
        Optional<String> providerUrl = provider.getUrl();
        if (providerUrl.isPresent()) {
            queryBuilder = createQueryWithProviderUrl(provider, project, providerUrl.get());
        } else {
            queryBuilder = createQueryWithoutProviderUrl(provider, project);
        }

        for (AzureSearchFieldMappingBuilder.ReferenceToValue refToValue : fieldReferenceNameToExpectedValue.buildAsList()) {
            queryBuilder = queryBuilder.and(refToValue.getReferenceKey(), WorkItemQueryWhereOperator.EQ, refToValue.getFieldValue());
        }

        WorkItemQuery query = queryBuilder.orderBy(WorkItemResponseFields.System_Id.getFieldName()).build();
        return queryManager.executeQueryAndRetrieveWorkItems(query);
    }

    private WorkItemQueryWhere createQueryWithProviderUrl(LinkableItem provider, LinkableItem project, String providerUrl) {
        String providerKey = provider.getLabel();
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(project);
        String oldProviderKey = AzureBoardsSearchPropertiesUtils.createProviderKeyWithUrl(providerKey, providerUrl);

        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();

        return WorkItemQuery
            .select(systemIdFieldName)
            .fromWorkItems()
            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
            .beginAndGroup()
            .beginGroup()
            .condition(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
            .and(AzureCustomFieldManager.ALERT_PROVIDER_URL_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerUrl)
            .endGroup()
            .or(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, oldProviderKey)
            .or(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.CONTAINS, providerKey)
            .endGroup()
            .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey)
            .endGroupToWhereClause();
    }

    private WorkItemQueryWhere createQueryWithoutProviderUrl(LinkableItem provider, LinkableItem project) {
        String providerKey = provider.getLabel();
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(project);
        String oldProviderKey = AzureBoardsSearchPropertiesUtils.createProviderKeyWithUrl(providerKey, null);

        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();

        return WorkItemQuery
            .select(systemIdFieldName)
            .fromWorkItems()
            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
            .beginAndGroup()
            .condition(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
            .or(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, oldProviderKey)
            .or(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.CONTAINS, providerKey)
            .endGroup()
            .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey)
            .endGroupToWhereClause();
    }
}
