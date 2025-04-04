/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.model.AzureArrayResponseModel;
import com.blackduck.integration.alert.azure.boards.common.service.query.AzureWorkItemQueryService;
import com.blackduck.integration.alert.azure.boards.common.service.query.WorkItemQueryResultResponseModel;
import com.blackduck.integration.alert.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.AzureWorkItemService;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.WorkItemReferenceModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

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
