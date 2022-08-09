/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;

import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsWorkItemSearchResult {
    private final WorkItemQuery query;
    private final List<WorkItemResponseModel> searchResults;

    public AzureBoardsWorkItemSearchResult(WorkItemQuery query, List<WorkItemResponseModel> searchResults) {
        this.query = query;
        this.searchResults = searchResults;
    }

    public WorkItemQuery getQuery() {
        return query;
    }

    public List<WorkItemResponseModel> getSearchResults() {
        return searchResults;
    }
}
