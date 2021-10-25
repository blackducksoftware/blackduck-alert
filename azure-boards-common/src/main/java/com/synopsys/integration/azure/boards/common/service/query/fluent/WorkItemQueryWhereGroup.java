/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public class WorkItemQueryWhereGroup implements WorkItemQueryWhereItem {
    private final WorkItemQueryWhere workItemQueryWhere;
    private final WorkItemQueryWhereGroup parentGroup;
    private final WorkItemQueryWhereJunctionType junctionType;
    private final List<WorkItemQueryWhereItem> items;

    /*package private */ WorkItemQueryWhereGroup(WorkItemQueryWhere workItemQueryWhere, @Nullable WorkItemQueryWhereGroup parentGroup, WorkItemQueryWhereJunctionType junctionType,
        List<WorkItemQueryWhereItem> items) {
        this.workItemQueryWhere = workItemQueryWhere;
        this.parentGroup = parentGroup;
        this.junctionType = junctionType;
        this.items = items;
    }

    public WorkItemQueryWhereGroup condition(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return condition(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhereGroup condition(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, null);
        items.add(condition);
        return this;
    }

    public WorkItemQueryWhereGroup and(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return and(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhereGroup and(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.AND);
        items.add(condition);
        return this;
    }

    public WorkItemQueryWhereGroup or(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return or(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhereGroup or(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.OR);
        items.add(condition);
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

    public WorkItemQueryWhereGroup endGroup() {
        return getParentGroup().orElse(this);
    }

    public WorkItemQueryWhere endGroupToWhereClause() {
        return workItemQueryWhere;
    }

    private WorkItemQueryWhereGroup beginGroup(@Nullable WorkItemQueryWhereJunctionType junctionType) {
        WorkItemQueryWhereGroup newGroup = new WorkItemQueryWhereGroup(workItemQueryWhere, this, junctionType, new LinkedList<>());
        this.items.add(newGroup);
        return newGroup;
    }

    protected Optional<WorkItemQueryWhereGroup> getParentGroup() {
        return Optional.ofNullable(parentGroup);
    }

    @Override
    public Optional<WorkItemQueryWhereJunctionType> getJunction() {
        return Optional.ofNullable(junctionType);
    }

    @Override
    public String toString() {
        StringBuilder groupBuilder = new StringBuilder();

        groupBuilder.append("(");
        for (WorkItemQueryWhereItem condition : items) {
            Optional<WorkItemQueryWhereJunctionType> junctionType = condition.getJunction();

            if (junctionType.isPresent()) {
                groupBuilder.append(' ');
                groupBuilder.append(junctionType.get());
            }
            groupBuilder.append(' ');
            groupBuilder.append(condition);
        }
        groupBuilder.append(" )");

        return groupBuilder.toString();
    }
}
