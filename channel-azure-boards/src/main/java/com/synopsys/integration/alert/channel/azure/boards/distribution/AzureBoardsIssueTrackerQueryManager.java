/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.synopsys.integration.azure.boards.common.service.query.WorkItemQueryResultResponseModel;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemReferenceModel;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsIssueTrackerQueryManager {
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemService azureWorkItemService;
    private final AzureWorkItemQueryService azureWorkItemQueryService;

    public AzureBoardsIssueTrackerQueryManager(
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemService azureWorkItemService,
        AzureWorkItemQueryService azureWorkItemQueryService
    ) {
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.azureWorkItemService = azureWorkItemService;
        this.azureWorkItemQueryService = azureWorkItemQueryService;
    }

    public List<WorkItemResponseModel> executeQueryAndRetrieveWorkItems(WorkItemQuery query) throws AlertException {
        WorkItemQueryResultResponseModel workItemQueryResultResponseModel = executeQuery(query);

        Set<Integer> workItemIds = workItemQueryResultResponseModel.getWorkItems()
                                       .stream()
                                       .map(WorkItemReferenceModel::getId)
                                       .collect(Collectors.toSet());
        if (!workItemIds.isEmpty()) {
            return retrieveWorkItems(workItemIds);
        }
        return List.of();
    }

    private WorkItemQueryResultResponseModel executeQuery(WorkItemQuery query) throws AlertException {
        try {
            return azureWorkItemQueryService.queryForWorkItems(organizationName, distributionDetails.getProjectNameOrId(), query);
        } catch (HttpServiceException e) {
            throw new AlertException("Failed to query for work items in Azure Boards", e);
        }
    }

    private List<WorkItemResponseModel> retrieveWorkItems(Set<Integer> workItemIds) throws AlertException {
        try {
            AzureArrayResponseModel<WorkItemResponseModel> workItemArrayResponse = azureWorkItemService.getWorkItems(organizationName, distributionDetails.getProjectNameOrId(), workItemIds);
            return workItemArrayResponse.getValue();
        } catch (HttpServiceException e) {
            throw new AlertException("Failed to retrieve work items from Azure Boards", e);
        }
    }

}
