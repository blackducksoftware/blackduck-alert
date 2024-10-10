/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.workitem.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldDefinition;
import com.google.gson.JsonObject;

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
