package com.synopsys.integration.alert.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.provider.state.ProviderProperties;

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
