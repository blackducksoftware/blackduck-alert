/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
