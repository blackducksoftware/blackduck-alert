/**
 * azure-boards-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

public enum WorkItemQueryWhereOperator {
    EQ("="),
    NOT_EQUALS("<>"),
    GT(">"),
    GT_EQ(">="),
    LT("<"),
    LT_EQ("<="),
    IN("In"),
    NOT_IN("Not In"),
    WAS_EVER("Was Ever"),
    CONTAINS("Contains"),
    DOES_NOT_CONTAIN("Does Not Contain"),
    IN_GROUP("In Group"),
    NOT_IN_GROUP("Not In Group"),
    CONTAINS_WORDS("Contains Words"),
    DOES_NOT_CONTAIN_WORDS("Does Not Contain Words"),
    IS_EMPTY("Is Empty"),
    IS_NOT_EMPTY("Is Not Empty"),
    UNDER("Under"),
    NOT_UNDER("Not Under");

    private final String comparator;

    WorkItemQueryWhereOperator(String comparator) {
        this.comparator = comparator;
    }

    public String getComparator() {
        return comparator;
    }

}
