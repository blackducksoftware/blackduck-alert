package com.blackduck.integration.alert.azure.boards.common.service.workitem.response;

import java.util.Optional;

import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldDefinition;
import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldsExtractor;
import com.google.gson.JsonObject;

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
