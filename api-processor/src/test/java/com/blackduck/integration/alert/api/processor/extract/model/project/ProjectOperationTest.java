/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.blackduck.api.manual.enumeration.OperationType;

public class ProjectOperationTest {
    @Test
    public void fromOperationTypeCreateTest() {
        assertEquals(ProjectOperation.CREATE, ProjectOperation.fromOperationType(OperationType.CREATE));
    }

    @Test
    public void fromOperationTypeDeleteTest() {
        assertEquals(ProjectOperation.DELETE, ProjectOperation.fromOperationType(OperationType.DELETE));
    }

    @Test
    public void fromOperationTypeUpdateTest() {
        assertEquals(ProjectOperation.CREATE, ProjectOperation.fromOperationType(OperationType.UPDATE));
    }
}
