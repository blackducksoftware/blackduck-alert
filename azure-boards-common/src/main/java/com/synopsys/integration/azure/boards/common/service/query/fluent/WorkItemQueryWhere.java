/**
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class WorkItemQueryWhere {
    private final boolean grouped;
    private final WorkItemQueryFrom workItemQueryFrom;
    private final List<WorkItemQueryWhereCondition> conditions;

    /* package-private */ WorkItemQueryWhere(boolean grouped, WorkItemQueryFrom workItemQueryFrom, WorkItemQueryWhereCondition condition) {
        this.workItemQueryFrom = workItemQueryFrom;
        this.grouped = grouped;
        this.conditions = new ArrayList<>();
        this.conditions.add(condition);
    }

    /* package-private */ WorkItemQueryWhere(boolean grouped, WorkItemQueryFrom workItemQueryFrom, List<WorkItemQueryWhereCondition> conditions) {
        this.grouped = grouped;
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
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.AND, grouped);
        conditions.add(condition);
        return this;
    }

    public WorkItemQueryWhere or(String lhs, WorkItemQueryWhereOperator operator, String rhs) {
        return or(lhs, operator, rhs, WorkItemQueryWhereConditionRHSType.LITERAL);
    }

    public WorkItemQueryWhere or(String lhs, WorkItemQueryWhereOperator operator, String rhs, WorkItemQueryWhereConditionRHSType rhsType) {
        WorkItemQueryWhereCondition condition = new WorkItemQueryWhereCondition(lhs, operator, rhs, rhsType, WorkItemQueryWhereJunctionType.OR, grouped);
        conditions.add(condition);
        return this;
    }

    public WorkItemQueryWhere beginGroup() {
        return new WorkItemQueryWhere(true, workItemQueryFrom, conditions);
    }

    public WorkItemQueryWhere endGroup() {
        return new WorkItemQueryWhere(false, workItemQueryFrom, conditions);
    }

    /* package-private */ WorkItemQueryFrom getWorkItemQueryFrom() {
        return workItemQueryFrom;
    }

    /* package-private */ List<WorkItemQueryWhereCondition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append("WHERE");

        boolean inGroup = false;
        for (WorkItemQueryWhereCondition condition : conditions) {
            if (inGroup && !condition.isInGroup()) {
                whereBuilder.append(" )");
            }

            if (null != condition.getJunction()) {
                whereBuilder.append(' ');
                whereBuilder.append(condition.getJunction().name());
            }

            if (!inGroup && condition.isInGroup()) {
                whereBuilder.append(" (");
            }

            whereBuilder.append(' ');
            whereBuilder.append(formatCondition(condition));

            inGroup = condition.isInGroup();
        }

        // If the last condition was part of a group, close the group
        if (inGroup) {
            whereBuilder.append(" )");
        }
        return whereBuilder.toString();
    }

    private String formatCondition(WorkItemQueryWhereCondition condition) {
        String rhs;
        if (WorkItemQueryWhereOperator.NOT_EQUALS.equals(condition.getOperator())) {
            rhs = "''";
        } else {
            rhs = formatRhs(condition.getRhs(), condition.getRhsType());
        }
        return String.format("[%s] %s %s", condition.getLhs(), condition.getOperator().getComparator(), rhs);
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
