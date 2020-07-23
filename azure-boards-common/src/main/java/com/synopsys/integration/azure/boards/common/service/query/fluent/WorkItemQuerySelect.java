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
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.util.List;
import java.util.stream.Collectors;

public class WorkItemQuerySelect {
    private final List<String> fields;

    /* package-private */ WorkItemQuerySelect(List<String> fields) {
        this.fields = fields;
    }

    public WorkItemQuerySelect andSelect(String field) {
        fields.add(field);
        return this;
    }

    public WorkItemQueryFrom fromWorkItems() {
        return new WorkItemQueryFrom(this, WorkItemQueryFrom.FROM_WORK_ITEMS);
    }

    public WorkItemQueryFrom fromWorkItemLinks() {
        return new WorkItemQueryFrom(this, WorkItemQueryFrom.FROM_WORK_ITEM_LINKS);
    }

    /* package-private */ List<String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("SELECT ");
        String joinedFields = fields
                                  .stream()
                                  .map(field -> String.format("[%s]", field))
                                  .collect(Collectors.joining(", "));
        selectBuilder.append(joinedFields);
        return selectBuilder.toString();
    }

}
