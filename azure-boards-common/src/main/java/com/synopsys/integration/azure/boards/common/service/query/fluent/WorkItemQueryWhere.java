/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public class WorkItemQueryWhere {
    private final WorkItemQueryFrom workItemQueryFrom;
    private final List<WorkItemQueryWhereItem> conditions;

    /* package-private */ WorkItemQueryWhere(WorkItemQueryFrom workItemQueryFrom, WorkItemQueryWhereCondition condition) {
        this.workItemQueryFrom = workItemQueryFrom;
        this.conditions = new ArrayList<>();
        this.conditions.add(condition);
    }

    /* package-private */ WorkItemQueryWhere(WorkItemQueryFrom workItemQueryFrom, List<WorkItemQueryWhereItem> conditions) {
        this.workItemQueryFrom = workItemQueryFrom;
        this.conditions = conditions;
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

    public WorkItemQueryWhere and(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return and(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhere and(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.AND);
        conditions.add(condition);
        return this;
    }

    public WorkItemQueryWhere or(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return or(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhere or(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.OR);
        conditions.add(condition);
        return this;
    }

    public WorkItemQueryWhereGroup beginGroup() {
        return beginGroup(null);
    }

    public WorkItemQueryWhereGroup beginAndGroup() {
        return beginGroup(WorkItemQueryWhereJunctionType.AND);
    }

    public WorkItemQueryWhereGroup beginOrGroup() {
        return beginGroup(WorkItemQueryWhereJunctionType.OR);
    }

    private WorkItemQueryWhereGroup beginGroup(@Nullable WorkItemQueryWhereJunctionType junctionType) {
        WorkItemQueryWhereGroup newGroup = new WorkItemQueryWhereGroup(this, null, junctionType, new LinkedList<>());
        this.conditions.add(newGroup);
        return newGroup;
    }

    /* package-private */ WorkItemQueryFrom getWorkItemQueryFrom() {
        return workItemQueryFrom;
    }

    /* package-private */ List<WorkItemQueryWhereItem> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append("WHERE ");

        for (WorkItemQueryWhereItem condition : conditions) {
            Optional<WorkItemQueryWhereJunctionType> junctionType = condition.getJunction();

            if (junctionType.isPresent()) {
                whereBuilder.append(' ');
                whereBuilder.append(junctionType.get());
                whereBuilder.append(' ');
            }
            whereBuilder.append(condition);
        }

        return whereBuilder.toString();
    }
}
