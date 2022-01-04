/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
