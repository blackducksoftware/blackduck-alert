/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.blackduck.integration.alert.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;

public class WorkItemQueryTest {
    @Test
    public void toStringTest() {
        LocalDate asOfDate = LocalDate.of(2020, 1, 1);
        DateTimeFormatter asOfDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String expectedQueryString = "SELECT [Field] FROM WorkItems WHERE [Field] = 'a value' ORDER BY [Field] ASOF " + asOfDateFormat.format(asOfDate);
        WorkItemQuery workItemQuery = WorkItemQuery
                                          .select("Field")
                                          .fromWorkItems()
                                          .where("Field", WorkItemQueryWhereOperator.EQ, "a value")
                                          .orderBy("Field")
                                          .asOf(asOfDate);
        assertEquals(expectedQueryString, workItemQuery.toString());
    }

    @Test
    public void whereGroupTest() {
        String expectedQueryString = "SELECT [Field] FROM WorkItems WHERE ( [Field] = 'a value' OR [Other Field] Contains 'something else' ) ORDER BY [Other Field]";
        WorkItemQuery workItemQuery = WorkItemQuery
                                          .select("Field")
                                          .fromWorkItems()
                                          .whereGroup("Field", WorkItemQueryWhereOperator.EQ, "a value")
                                          .or("Other Field", WorkItemQueryWhereOperator.CONTAINS, "something else")
                                          .orderBy("Other Field")
                                          .build();
        assertEquals(expectedQueryString, workItemQuery.toString());
    }

    @Test
    public void whereMultiGroupTest() {
        String expectedQueryString = "SELECT [Field]"
                                         + " FROM WorkItems"
                                         + " WHERE ( [Field] = 'a value' OR [Other Field] Contains 'something else' )"
                                         + " AND [Thing] Was Ever 'not a thing'"
                                         + " AND ( [Something] Does Not Contain 'forbidden phrase' AND [Something Else] <> '' )"
                                         + " ORDER BY [Other Field]";
        WorkItemQuery workItemQuery = WorkItemQuery
                                          .select("Field")
                                          .fromWorkItems()
                                          .whereGroup("Field", WorkItemQueryWhereOperator.EQ, "a value")
                                          .or("Other Field", WorkItemQueryWhereOperator.CONTAINS, "something else")
                                          .endGroup()
                                          .and("Thing", WorkItemQueryWhereOperator.WAS_EVER, "not a thing")
                                          .beginGroup()
                                          .and("Something", WorkItemQueryWhereOperator.DOES_NOT_CONTAIN, "forbidden phrase")
                                          .and("Something Else", WorkItemQueryWhereOperator.NOT_EQUALS, null)
                                          .orderBy("Other Field")
                                          .build();
        assertEquals(expectedQueryString, workItemQuery.toString());
    }

}
