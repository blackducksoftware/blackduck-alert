package com.synopsys.integration.azure.boards.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.azure.boards.common.service.query.builder.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.query.builder.WorkItemQueryWhereCondition;

public class WorkItemQueryTest {
    @Test
    public void toStringTest() {
        LocalDate asOfDate = LocalDate.of(2020, 1, 1);
        DateTimeFormatter asOfDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String expectedQueryString = "SELECT [Field] FROM WorkItems WHERE [Field] = 'a value' ORDER BY [Field] ASOF " + asOfDateFormat.format(asOfDate);
        WorkItemQuery workItemQuery = WorkItemQuery
                                          .select("Field")
                                          .fromWorkItems()
                                          .where(WorkItemQueryWhereCondition.equals("Field", "a value"))
                                          .orderBy("Field")
                                          .asOf(asOfDate);
        assertEquals(expectedQueryString, workItemQuery.toString());
    }

}
