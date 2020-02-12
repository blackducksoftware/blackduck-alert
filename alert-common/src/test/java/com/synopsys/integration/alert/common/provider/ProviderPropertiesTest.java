package com.synopsys.integration.alert.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

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
    public void getConfigIdTest() throws AlertDatabaseConstraintException {
        Long id = 23L;
        ProviderProperties properties = new ProviderProperties(id) {
            @Override
            public void disconnect() {

            }
        };
        assertEquals(id, properties.getConfigId());
    }
}
