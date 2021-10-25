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

public class WorkItemQueryFrom {
    /* package-private */ static final String FROM_WORK_ITEMS = "WorkItems";
    /* package-private */ static final String FROM_WORK_ITEM_LINKS = "WorkItemLinks";

    private final WorkItemQuerySelect workItemQuerySelect;
    private final String from;

    /* package-private */ WorkItemQueryFrom(WorkItemQuerySelect workItemQuerySelect, String from) {
        this.workItemQuerySelect = workItemQuerySelect;
        this.from = from;
    }

    public WorkItemQueryWhere where(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return where(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhere where(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, null);
        return new WorkItemQueryWhere(this, condition);
    }

    public WorkItemQueryWhereGroup whereGroup(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return whereGroup(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhereGroup whereGroup(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, null);
        WorkItemQueryWhere workItemQueryWhere = new WorkItemQueryWhere(this, new LinkedList<>());
        List<WorkItemQueryWhereItem> conditions = new LinkedList<>();
        conditions.add(condition);
        WorkItemQueryWhereGroup group = new WorkItemQueryWhereGroup(workItemQueryWhere, null, null, conditions);
        workItemQueryWhere.getConditions().add(group);
        return group;
    }

    /* package-private */ WorkItemQuerySelect getWorkItemQuerySelect() {
        return workItemQuerySelect;
    }

    /* package-private */ String getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return String.format("FROM %s", from);
    }

}
