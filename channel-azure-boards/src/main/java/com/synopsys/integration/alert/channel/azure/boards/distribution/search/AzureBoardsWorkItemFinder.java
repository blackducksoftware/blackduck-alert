/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String providerKey = provider.getLabel();
        Optional<String> providerUrl = provider.getUrl();
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(project);

        WorkItemQueryWhere oldProviderQueryBuilder = createOldProviderQuery(provider, topicKey);
        WorkItemQueryWhere newProviderQueryBuilder = createNewProviderQuery(provider, topicKey);

        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        //        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();
        //        WorkItemQueryWhere queryBuilder = WorkItemQuery
        //            .select(systemIdFieldName)
        //            .fromWorkItems()
        //            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
        //            .and(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
        //            .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey);

        for (AzureSearchFieldMappingBuilder.ReferenceToValue refToValue : fieldReferenceNameToExpectedValue.buildAsList()) {
            oldProviderQueryBuilder = oldProviderQueryBuilder.and(refToValue.getReferenceKey(), WorkItemQueryWhereOperator.EQ, refToValue.getFieldValue());
            newProviderQueryBuilder = newProviderQueryBuilder.and(refToValue.getReferenceKey(), WorkItemQueryWhereOperator.EQ, refToValue.getFieldValue());
        }

        WorkItemQuery oldProviderQuery = oldProviderQueryBuilder.orderBy(systemIdFieldName).build();
        WorkItemQuery newProviderQuery = newProviderQueryBuilder.orderBy(systemIdFieldName).build();
        List<WorkItemResponseModel> responses = new LinkedList<>();
        List<WorkItemResponseModel> oldProviderResponses = queryManager.executeQueryAndRetrieveWorkItems(oldProviderQuery);
        List<WorkItemResponseModel> newProviderResponses = queryManager.executeQueryAndRetrieveWorkItems(newProviderQuery);
        Stream.concat(oldProviderResponses.stream(), newProviderResponses.stream())
            .collect(Collectors.toMap(WorkItemResponseModel::getId, Function.identity()))
            .values().stream()
            .sorted(Comparator.comparing(WorkItemResponseModel::getId))
            .collect(Collectors.toList());

        return responses;
    }

    @Deprecated(since = "6.8.0", forRemoval = true)
    //TODO remove this method in 8.0.0
    private WorkItemQueryWhere createOldProviderQuery(LinkableItem provider, String topicKey) {
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKeyWithUrl(provider.getLabel(), provider.getUrl().orElse(null));
        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();
        WorkItemQueryWhere queryBuilder = WorkItemQuery
            .select(systemIdFieldName)
            .fromWorkItems()
            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
            .and(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
            .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey);
        return queryBuilder;
    }

    private WorkItemQueryWhere createNewProviderQuery(LinkableItem provider, String topicKey) {
        String providerKey = provider.getLabel();
        Optional<String> providerUrl = provider.getUrl();
        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();
        WorkItemQueryWhere queryBuilder = WorkItemQuery
            .select(systemIdFieldName)
            .fromWorkItems()
            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
            .and(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey);

        if (providerUrl.isPresent()) {
            queryBuilder = queryBuilder.and(AzureCustomFieldManager.ALERT_PROVIDER_URL_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerUrl.get());
        }
        queryBuilder = queryBuilder.and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey);
        return queryBuilder;
    }

}
