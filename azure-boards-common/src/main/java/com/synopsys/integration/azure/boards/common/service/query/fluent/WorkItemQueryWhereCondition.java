/*
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

import javax.annotation.Nullable;

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
