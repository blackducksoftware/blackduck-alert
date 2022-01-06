/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
