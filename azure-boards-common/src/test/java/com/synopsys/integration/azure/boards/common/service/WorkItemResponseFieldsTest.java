package com.synopsys.integration.azure.boards.common.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemResponseFieldsTest {
    @Test
    public void listTest() {
        List<AzureFieldDefinition> azureFieldDefinitions = WorkItemResponseFields.list();
        assertTrue(!azureFieldDefinitions.isEmpty(), "Expected the fields list to not be empty");
    }

}
