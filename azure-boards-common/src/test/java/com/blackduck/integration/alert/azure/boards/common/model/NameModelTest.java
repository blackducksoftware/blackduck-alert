/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class NameModelTest {
    @Test
    public void getNameTest() {
        String name = "Name";
        NameModel nameModel = new NameModel(name);
        assertEquals(name, nameModel.getName());
    }

    @Test
    public void emptyNameModelTest() {
        NameModel nameModel = new NameModel();
        assertNull(nameModel.getName());
    }
}
