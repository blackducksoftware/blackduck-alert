package com.synopsys.integration.alert.common.provider;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public class ProviderConfigMissingValidatorTest {
    @Test
    public void testMissingProviders() {
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        List<Provider> providers = List.of();
        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageUtility, providers, configurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageUtility, Mockito.never())
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.any(SystemMessageType.class));

    }

    @Test
    public void testEmptyConfigurationsList() throws Exception {
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(ProviderKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of());

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageUtility, providers, configurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageUtility)
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

    @Test
    public void testConfigurationsThrowException() throws Exception {
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(ProviderKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenThrow(AlertDatabaseConstraintException.class);

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageUtility, providers, configurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageUtility)
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

    @Test
    public void testEmptyConfigurations() throws Exception {
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        Mockito.when(model.getCopyOfFieldList()).thenReturn(List.of());
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(ProviderKey.class), Mockito.any(ConfigContextEnum.class)))
            .thenReturn(List.of(model));

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageUtility, providers, configurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageUtility)
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }

    @Test
    public void testValidConfigurations() throws Exception {
        SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        List<Provider> providers = List.of(Mockito.mock(Provider.class));
        ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        Mockito.when(model.getCopyOfFieldList()).thenReturn(List.of(Mockito.mock(ConfigurationFieldModel.class)));
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(), Mockito.any()))
            .thenReturn(List.of(model));

        ProviderConfigMissingValidator validator = new ProviderConfigMissingValidator(systemMessageUtility, providers, configurationAccessor);
        validator.validate();
        Mockito.verify(systemMessageUtility, Mockito.never())
            .addSystemMessage(Mockito.anyString(), Mockito.any(SystemMessageSeverity.class), Mockito.anyString());

    }
}
