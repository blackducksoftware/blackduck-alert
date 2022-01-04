/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.query.fluent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Based on the documentation found here: https://docs.microsoft.com/en-us/azure/devops/boards/queries/wiql-syntax
 */
public class WorkItemQuery {
    public static final int QUERY_CHAR_LIMIT = 32768;
    public static final DateTimeFormatter AS_OF_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final WorkItemQuerySelect select;
    private final WorkItemQueryFrom from;
    private final WorkItemQueryWhere where;
    private final WorkItemQueryOrderBy orderBy;
    private final LocalDate asOf;

    /* package-private */ WorkItemQuery(WorkItemQuerySelect select, WorkItemQueryFrom from, WorkItemQueryWhere where, WorkItemQueryOrderBy orderBy, @Nullable LocalDate asOf) {
        this.select = select;
        this.from = from;
        this.where = where;
        this.orderBy = orderBy;
        this.asOf = asOf;
    }

    public static WorkItemQuerySelect select(String field) {
        List<String> fields = new ArrayList<>();
        fields.add(field);
        return new WorkItemQuerySelect(fields);
    }

    public boolean exceedsCharLimit() {
        return toString().length() > QUERY_CHAR_LIMIT;
    }

    /**
     * Alias for {@link WorkItemQuery#toString}
     */
    public String rawQuery() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append(select.toString());
        queryStringBuilder.append(' ');
        queryStringBuilder.append(from.toString());

        if (null != where) {
            queryStringBuilder.append(' ');
            queryStringBuilder.append(where.toString());
        }

        if (null != orderBy) {
            queryStringBuilder.append(' ');
            queryStringBuilder.append(orderBy.toString());
        }

        if (null != asOf) {
            queryStringBuilder.append(" ASOF ");
            queryStringBuilder.append(AS_OF_FORMAT.format(asOf));
        }

        return queryStringBuilder.toString();
    }

}
