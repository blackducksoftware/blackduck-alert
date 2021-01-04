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
package com.synopsys.integration.azure.boards.common.service.workitem.response;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;
import com.synopsys.integration.azure.boards.common.util.AzureFieldsExtractor;

public class WorkItemFieldsWrapper {
    private final AzureFieldsExtractor fieldsExtractor;
    private final JsonObject workItemFields;

    public WorkItemFieldsWrapper(AzureFieldsExtractor fieldsExtractor, JsonObject workItemFields) {
        this.fieldsExtractor = fieldsExtractor;
        this.workItemFields = workItemFields;
    }

    public Optional<String> getTeamProject() {
        return getField(WorkItemResponseFields.System_TeamProject);
    }

    public Optional<String> getWorkItemType() {
        return getField(WorkItemResponseFields.System_WorkItemType);
    }

    public Optional<String> getState() {
        return getField(WorkItemResponseFields.System_State);
    }

    public Optional<String> getTitle() {
        return getField(WorkItemResponseFields.System_Title);
    }

    public Optional<String> getDescription() {
        return getField(WorkItemResponseFields.System_Description);
    }

    public <T> Optional<T> getField(AzureFieldDefinition<T> fieldDefinition) {
        return fieldsExtractor.extractField(workItemFields, fieldDefinition);
    }

}
