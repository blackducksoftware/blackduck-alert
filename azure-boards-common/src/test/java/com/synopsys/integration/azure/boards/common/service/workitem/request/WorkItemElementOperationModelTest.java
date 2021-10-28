package com.synopsys.integration.azure.boards.common.service.workitem.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemElementOperationModelTest {
    @Test
    public void fieldElementTest() {
        String value = "fieldValue";

        WorkItemElementOperation operation = WorkItemElementOperation.ADD;
        AzureFieldDefinition<String> fieldDefinition = WorkItemResponseFields.System_Title;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(WorkItemResponseFields.System_Title.getFieldName(), value);

        WorkItemElementOperationModel workItemElementOperationModel = WorkItemElementOperationModel.fieldElement(operation, fieldDefinition, jsonObject);

        assertEquals(operation.toLowerCaseString(), workItemElementOperationModel.getOp());
        assertEquals("/fields/" + fieldDefinition.getFieldName(), workItemElementOperationModel.getPath());
        assertNull(workItemElementOperationModel.getFrom());
        assertEquals(jsonObject, workItemElementOperationModel.getValue());
    }
}
