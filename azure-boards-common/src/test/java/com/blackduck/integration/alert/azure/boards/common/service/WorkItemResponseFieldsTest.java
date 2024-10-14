/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.blackduck.integration.alert.azure.boards.common.util.AzureFieldDefinition;

public class WorkItemResponseFieldsTest {
    @Test
    public void listTest() {
        List<AzureFieldDefinition> azureFieldDefinitions = WorkItemResponseFields.list();
        assertTrue(!azureFieldDefinitions.isEmpty(), "Expected the fields list to not be empty");
    }

}
