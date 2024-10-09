/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.SystemMessageSeverity;
import com.blackduck.integration.alert.common.enumeration.SystemMessageType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

public class ProviderConfigMissingValidatorTest {
    @Test
    public void testMissingProviders() {
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        List<Provider> providers = List.of();
        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageAccessor, providers, configurationModelConfigurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageAccessor, Mockito.never())
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));

    }

    @Test
    public void testEmptyConfigurationsList() {
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(ProviderKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of());

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageAccessor, providers, configurationModelConfigurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageAccessor)
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

    @Test
    public void testEmptyConfigurations() {
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        Mockito.when(model.getCopyOfFieldList()).thenReturn(List.of());
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(ProviderKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(model));

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageAccessor, providers, configurationModelConfigurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageAccessor)
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

    @Test
    public void testValidConfigurations() {
        SystemMessageAccessor systemMessageAccessor = Mockito.mock(SystemMessageAccessor.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        Mockito.when(model.getCopyOfFieldList()).thenReturn(List.of(Mockito.mock(ConfigurationFieldModel.class)));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(), Mockito.any()))
            .thenReturn(List.of(model));

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageAccessor, providers, configurationModelConfigurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageAccessor, Mockito.never())
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

}
