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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
