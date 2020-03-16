package com.synopsys.integration.alert.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;

public class ProviderPropertiesTest {
    private static final ProviderKey PROVIDER_KEY = new ProviderKey() {
        @Override
        public String getUniversalKey() {
            return "provider_name";
        }

        @Override
        public String getDisplayName() {
            return "provider";
        }
    };

    private final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
    private ConfigurationModel configurationModel;

    @BeforeEach
    public void init() {
        configurationModel = Mockito.mock(ConfigurationModel.class);
    }

    @Test
    public void getConfigIdTest() {
        Long id = 23L;
        FieldAccessor fieldAccessor = new FieldAccessor(Map.of());
        ProviderProperties properties = new ProviderProperties(id, fieldAccessor) {};
        assertEquals(id, properties.getConfigId());
    }

}
