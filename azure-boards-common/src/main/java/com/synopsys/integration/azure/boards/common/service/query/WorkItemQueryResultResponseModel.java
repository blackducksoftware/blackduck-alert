/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
