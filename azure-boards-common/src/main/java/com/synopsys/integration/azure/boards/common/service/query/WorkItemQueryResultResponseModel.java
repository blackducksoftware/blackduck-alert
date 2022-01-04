/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query;

import java.util.List;

import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemFieldReferenceModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemLinkModel;
import com.synopsys.integration.azure.boards.common.service.workitem.WorkItemReferenceModel;

public class WorkItemQueryResultResponseModel {
    private String queryType;
    private String queryResultType;
    private List<WorkItemFieldReferenceModel> columns;
    private List<WorkItemQuerySortColumnModel> sortColumns;
    private List<WorkItemReferenceModel> workItems;
    private List<WorkItemLinkModel> workItemRelations;
    private String asOf;

    public WorkItemQueryResultResponseModel() {
        // For serialization
    }

    public WorkItemQueryResultResponseModel(String queryType, String queryResultType, List<WorkItemFieldReferenceModel> columns,
        List<WorkItemQuerySortColumnModel> sortColumns, List<WorkItemReferenceModel> workItems, List<WorkItemLinkModel> workItemRelations, String asOf) {
        this.queryType = queryType;
        this.queryResultType = queryResultType;
        this.columns = columns;
        this.sortColumns = sortColumns;
        this.workItems = workItems;
        this.workItemRelations = workItemRelations;
        this.asOf = asOf;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getQueryResultType() {
        return queryResultType;
    }

    public List<WorkItemFieldReferenceModel> getColumns() {
        return columns;
    }

    public List<WorkItemQuerySortColumnModel> getSortColumns() {
        return sortColumns;
    }

    public List<WorkItemReferenceModel> getWorkItems() {
        return workItems;
    }

    public List<WorkItemLinkModel> getWorkItemRelations() {
        return workItemRelations;
    }

    public String getAsOf() {
        return asOf;
    }

}
