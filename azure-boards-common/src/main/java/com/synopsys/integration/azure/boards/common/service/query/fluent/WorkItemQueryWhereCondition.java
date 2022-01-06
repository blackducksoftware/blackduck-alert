/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import org.jetbrains.annotations.Nullable;

public class WorkItemQueryWhereCondition {
    private final String lhs;
    private final WorkItemQueryWhereOperator operator;
    private final String rhs;
    private final WorkItemQueryWhereConditionRHSType rhsType;
    private final WorkItemQueryWhereJunctionType junction;
    private final boolean inGroup;

    /* package-private */ WorkItemQueryWhereCondition(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType, @Nullable WorkItemQueryWhereJunctionType junction, boolean inGroup) {
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
        this.rhsType = rhsType;
        this.junction = junction;
        this.inGroup = inGroup;
    }

    public String getLhs() {
        return lhs;
    }

    public WorkItemQueryWhereOperator getOperator() {
        return operator;
    }

    public String getRhs() {
        return rhs;
    }

    public WorkItemQueryWhereConditionRHSType getRhsType() {
        return rhsType;
    }

    public WorkItemQueryWhereJunctionType getJunction() {
        return junction;
    }

    public boolean isInGroup() {
        return inGroup;
    }

}
