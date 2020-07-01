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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkItemQueryOrderBy {
    private final WorkItemQueryWhere workItemQueryWhere;
    private final List<WorkItemOrderByField> fields;

    /* package-private */ WorkItemQueryOrderBy(WorkItemQueryWhere workItemQueryWhere, WorkItemOrderByField field) {
        this.workItemQueryWhere = workItemQueryWhere;
        this.fields = new ArrayList<>();
        this.fields.add(field);
    }

    public WorkItemQueryOrderBy andOrderBy(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName);
        fields.add(field);
        return this;
    }

    public WorkItemQueryOrderBy andOrderByAsc(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName, WorkItemOrderByDirection.ASC);
        fields.add(field);
        return this;
    }

    public WorkItemQueryOrderBy andOrderByDesc(String fieldName) {
        WorkItemOrderByField field = new WorkItemOrderByField(fieldName, WorkItemOrderByDirection.DESC);
        fields.add(field);
        return this;
    }

    public WorkItemQuery asOf(LocalDate date) {
        WorkItemQueryFrom from = workItemQueryWhere.getWorkItemQueryFrom();
        WorkItemQuerySelect select = from.getWorkItemQuerySelect();
        return new WorkItemQuery(select, from, workItemQueryWhere, this, date);
    }

    public WorkItemQuery build() {
        return asOf(null);
    }

    /* package-private */ WorkItemQueryWhere getWorkItemQueryWhere() {
        return workItemQueryWhere;
    }

    /* package-private */ List<WorkItemOrderByField> getFields() {
        return fields;
    }

}
