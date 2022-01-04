/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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

    @Override
    public String toString() {
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("ORDER BY ");
        String joinedFields = fields
                                  .stream()
                                  .map(this::formatField)
                                  .collect(Collectors.joining(", "));
        orderByBuilder.append(joinedFields);
        return orderByBuilder.toString();
    }

    private String formatField(WorkItemOrderByField field) {
        Optional<String> optionalDirectionName = field.getDirection()
                                                     .map(WorkItemOrderByDirection::name)
                                                     .map(StringUtils::lowerCase)
                                                     .map(StringUtils::capitalize);
        if (optionalDirectionName.isPresent()) {
            return String.format("[%s] %s", field.getFieldName(), optionalDirectionName.get());
        }
        return String.format("[%s]", field.getFieldName());
    }

}
