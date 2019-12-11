package com.synopsys.integration.alert.common.provider;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
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
    public void getGlobalConfigTest() throws AlertDatabaseConstraintException {
        ProviderProperties properties = new ProviderProperties(PROVIDER_KEY, configurationAccessor) {};

        Mockito.when(configurationAccessor.getConfigurationByDescriptorKeyAndContext(PROVIDER_KEY, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));
        assertTrue(properties.retrieveGlobalConfig().isPresent());
    }

    @Test
    public void getGlobalConfigNotPresentTest() throws AlertDatabaseConstraintException {
        ProviderProperties properties = new ProviderProperties(PROVIDER_KEY, configurationAccessor) {};

        Mockito.when(configurationAccessor.getConfigurationByDescriptorKeyAndContext(PROVIDER_KEY, ConfigContextEnum.GLOBAL)).thenReturn(null);
        assertTrue(properties.retrieveGlobalConfig().isEmpty());

        Mockito.when(configurationAccessor.getConfigurationByDescriptorKeyAndContext(PROVIDER_KEY, ConfigContextEnum.GLOBAL)).thenReturn(List.of());
        assertTrue(properties.retrieveGlobalConfig().isEmpty());
    }

    @Test
    public void getGlobalConfigThrowsExceptionTest() throws AlertDatabaseConstraintException {
        ProviderProperties properties = new ProviderProperties(PROVIDER_KEY, configurationAccessor) {};

        Mockito.when(configurationAccessor.getConfigurationByDescriptorKeyAndContext(PROVIDER_KEY, ConfigContextEnum.GLOBAL)).thenThrow(new AlertDatabaseConstraintException("Fake constraint violated"));
        assertTrue(properties.retrieveGlobalConfig().isEmpty());
    }

}
