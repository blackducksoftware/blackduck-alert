package com.synopsys.integration.alert.provider.polaris;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;

public class PolarisPropertiesTest {
    private static final String POLARIS_URL = "https://polaris";
    private static final Integer POLARIS_TIMEOUT = 100;

    private final BaseConfigurationAccessor configurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
    private final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);

    @Test
    public void getUrlTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_URL);
        field.setFieldValue(POLARIS_URL);

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(PolarisDescriptor.KEY_POLARIS_URL, field)
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor);
        assertEquals(POLARIS_URL, polarisProperties.getUrl().orElse(null));
    }

    @Test
    public void getUrlWhenEmptyTest() throws AlertDatabaseConstraintException {
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of()
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor);
        assertEquals(Optional.empty(), polarisProperties.getUrl());
    }

    @Test
    public void getTimeoutTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_TIMEOUT);
        field.setFieldValue(POLARIS_TIMEOUT.toString());

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(PolarisDescriptor.KEY_POLARIS_TIMEOUT, field)
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor);
        assertEquals(POLARIS_TIMEOUT, polarisProperties.getTimeout());
    }

    @Test
    public void getTimeoutDefaultTest() throws AlertDatabaseConstraintException {
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of()
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor);
        assertEquals(PolarisProperties.DEFAULT_TIMEOUT, polarisProperties.getTimeout());
    }
}
