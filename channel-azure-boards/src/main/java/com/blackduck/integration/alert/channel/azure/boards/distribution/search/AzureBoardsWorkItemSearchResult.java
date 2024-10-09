package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import java.util.List;

import com.blackduck.integration.alert.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;

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
