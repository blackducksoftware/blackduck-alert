/**
 * azure-boards-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.azure.boards.common.service.query.builder;

import java.util.ArrayList;
import java.util.List;

public class WorkItemQueryWhere {
    private final WorkItemQueryFrom workItemQueryFrom;
    private final List<WorkItemQueryWhereCondition> conditions;
    // FIXME need way to distinguish between AND and OR
    //       and to group conditions

    /* package-private */ WorkItemQueryWhere(WorkItemQueryFrom workItemQueryFrom, WorkItemQueryWhereCondition condition) {
        this.workItemQueryFrom = workItemQueryFrom;
        this.conditions = new ArrayList<>();
        this.conditions.add(condition);
    }

    public WorkItemQueryOrderBy orderBy(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName);
        return new WorkItemQueryOrderBy(this, field);
    }

    public WorkItemQueryOrderBy orderByAsc(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName, WorkItemOrderByDirection.ASC);
        return new WorkItemQueryOrderBy(this, field);
    }

    public WorkItemQueryOrderBy orderByDesc(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName, WorkItemOrderByDirection.DESC);
        return new WorkItemQueryOrderBy(this, field);
    }

    public WorkItemQueryWhere and() {
        // FIXME implement
        return null;
    }

    public WorkItemQueryWhere or() {
        // FIXME implement
        return null;
    }

    /* package-private */ WorkItemQueryFrom getWorkItemQueryFrom() {
        return workItemQueryFrom;
    }

    /* package-private */ List<WorkItemQueryWhereCondition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append("WHERE ");
        // FIXME implement
        return whereBuilder.toString();
    }
}
