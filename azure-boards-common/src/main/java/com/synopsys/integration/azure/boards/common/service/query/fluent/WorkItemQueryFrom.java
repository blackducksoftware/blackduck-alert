/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

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
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, null, false);
        return new WorkItemQueryWhere(false, this, condition);
    }

    public WorkItemQueryWhere whereGroup(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return whereGroup(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhere whereGroup(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, null, true);
        return new WorkItemQueryWhere(true, this, condition);
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
