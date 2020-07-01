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
package com.synopsys.integration.azure.boards.common.service.query.builder;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class WorkItemQuery {
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

    public static WorkItemQuerySelect select(String field1, String... additionalFields) {
        List<String> fields = Stream.of(field1, additionalFields)
                                  .map(String.class::cast)
                                  .collect(Collectors.toList());
        return new WorkItemQuerySelect(fields);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
