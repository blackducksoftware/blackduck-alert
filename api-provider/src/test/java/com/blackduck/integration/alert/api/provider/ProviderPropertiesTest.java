/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.provider.state.ProviderProperties;

public class ProviderPropertiesTest {
    @Test
    public void getConfigIdTest() {
        Long id = 23L;
        boolean isEnabled = true;
        String configName = "A config";
        ProviderProperties properties = new ProviderProperties(id, isEnabled, configName) {};
        assertEquals(id, properties.getConfigId());
        assertEquals(isEnabled, properties.isConfigEnabled());
        assertEquals(configName, properties.getConfigName());
    }

}
