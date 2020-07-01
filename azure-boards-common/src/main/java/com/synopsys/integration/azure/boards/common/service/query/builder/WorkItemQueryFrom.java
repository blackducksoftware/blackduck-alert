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

public class WorkItemQueryFrom {
    private final WorkItemQuerySelect workItemQuerySelect;
    private final String from;

    /* package-private */ WorkItemQueryFrom(WorkItemQuerySelect workItemQuerySelect, String from) {
        this.workItemQuerySelect = workItemQuerySelect;
        this.from = from;
    }

    public WorkItemQueryWhere where(WorkItemQueryWhereCondition condition) {
        return new WorkItemQueryWhere(this, condition);
    }

    /* package-private */ WorkItemQuerySelect getWorkItemQuerySelect() {
        return workItemQuerySelect;
    }

    /* package-private */ String getFrom() {
        return from;
    }

}
