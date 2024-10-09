/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.web.api.about.AboutDescriptorModel;

public class AboutDescriptorModelTest {

    @Test
    public void testGetters() {
        final String iconName = "icon-name";
        final String name = "descriptor-name";
        AboutDescriptorModel model = new AboutDescriptorModel(iconName, name);
        assertEquals(iconName, model.getIconKey());
        assertEquals(name, model.getName());
    }
}
