package com.synopsys.integration.azure.boards.common.service.workitem.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemRequestTest {
    @Test
    public void getElementOperationModels() {
        String value = "fieldValue";

        WorkItemElementOperation operation = WorkItemElementOperation.ADD;
        AzureFieldDefinition<String> fieldDefinition = WorkItemResponseFields.System_Title;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(WorkItemResponseFields.System_Title.getFieldName(), value);

        WorkItemElementOperationModel workItemElementOperationModel = WorkItemElementOperationModel.fieldElement(operation, fieldDefinition, jsonObject);

        WorkItemRequest workItemRequest = new WorkItemRequest(List.of(workItemElementOperationModel));
        assertEquals(1, workItemRequest.getElementOperationModels().size());
        assertEquals(workItemElementOperationModel, workItemRequest.getElementOperationModels().get(0));
    }
}
