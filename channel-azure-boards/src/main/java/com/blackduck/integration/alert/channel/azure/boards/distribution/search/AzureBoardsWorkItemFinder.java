/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;

import com.blackduck.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.blackduck.integration.alert.channel.azure.boards.distribution.util.AzureBoardsSearchPropertiesUtils;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.alert.azure.boards.common.service.query.fluent.WorkItemQueryWhere;
import com.synopsys.integration.alert.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;
import com.synopsys.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class AzureBoardsWorkItemFinder {
    private final AzureBoardsIssueTrackerQueryManager queryManager;
    private final String teamProjectName;

    public AzureBoardsWorkItemFinder(AzureBoardsIssueTrackerQueryManager queryManager, String teamProjectName) {
        this.queryManager = queryManager;
        this.teamProjectName = teamProjectName;
    }

    public AzureBoardsWorkItemSearchResult findWorkItems(LinkableItem provider, LinkableItem project, AzureSearchFieldMappingBuilder fieldReferenceNameToExpectedValue)
        throws AlertException {
        String providerKey = AzureBoardsSearchPropertiesUtils.createProviderKey(provider.getLabel(), provider.getUrl().orElse(null));
        String topicKey = AzureBoardsSearchPropertiesUtils.createNullableLinkableItemKey(project);

        String systemIdFieldName = WorkItemResponseFields.System_Id.getFieldName();
        String teamProjectFieldName = WorkItemResponseFields.System_TeamProject.getFieldName();
        WorkItemQueryWhere queryBuilder = WorkItemQuery
            .select(systemIdFieldName)
            .fromWorkItems()
            .whereGroup(teamProjectFieldName, WorkItemQueryWhereOperator.EQ, teamProjectName)
            .and(AzureCustomFieldManager.ALERT_PROVIDER_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, providerKey)
            .and(AzureCustomFieldManager.ALERT_TOPIC_KEY_FIELD_REFERENCE_NAME, WorkItemQueryWhereOperator.EQ, topicKey);

        for (AzureSearchFieldMappingBuilder.ReferenceToValue refToValue : fieldReferenceNameToExpectedValue.buildAsList()) {
            queryBuilder = queryBuilder.and(refToValue.getReferenceKey(), WorkItemQueryWhereOperator.EQ, refToValue.getFieldValue());
        }

        WorkItemQuery query = queryBuilder.orderBy(systemIdFieldName).build();
        List<WorkItemResponseModel> searchResults = queryManager.executeQueryAndRetrieveWorkItems(query);
        return new AzureBoardsWorkItemSearchResult(query, searchResults);
    }

}
