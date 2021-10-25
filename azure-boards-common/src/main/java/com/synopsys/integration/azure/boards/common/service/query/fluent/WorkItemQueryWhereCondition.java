/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class WorkItemQueryWhereCondition implements WorkItemQueryWhereItem {
    private final String lhs;
    private final WorkItemQueryWhereOperator operator;
    private final String rhs;
    private final WorkItemQueryWhereConditionRHSType rhsType;
    private final WorkItemQueryWhereJunctionType junction;

    /* package-private */ WorkItemQueryWhereCondition(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType, @Nullable WorkItemQueryWhereJunctionType junction) {
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
        this.rhsType = rhsType;
        this.junction = junction;
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

    @Override
    public Optional<WorkItemQueryWhereJunctionType> getJunction() {
        return Optional.ofNullable(junction);
    }

    @Override
    public String toString() {
        return formatCondition();
    }

    private String formatCondition() {
        String rhs;
        if (WorkItemQueryWhereOperator.NOT_EQUALS.equals(getOperator())) {
            rhs = "''";
        } else {
            rhs = formatRhs(getRhs(), getRhsType());
        }
        return String.format("[%s] %s %s", getLhs(), getOperator().getComparator(), rhs);
    }

    private String formatRhs(String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        switch (rhsType) {
            case FIELD:
                return String.format("[%s]", rhs);
            case MACRO:
                return StringUtils.startsWith(rhs, "@") ? rhs : String.format("@%s", rhs);
            case LITERAL:
            default:
                return String.format("'%s'", rhs);
        }
    }
}
