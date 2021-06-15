package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckGlobalConfigurationValidator;

public class BlackDuckDescriptorTest {
    @Test
    public void testGetDefinedFields() {
        EncryptionSettingsValidator encryptionValidator = Mockito.mock(EncryptionSettingsValidator.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(encryptionValidator.apply(Mockito.any(), Mockito.any())).thenReturn(ValidationResult.success());
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(blackDuckContent, configurationAccessor);
        blackDuckDistributionUIConfig.setConfigFields();
        BlackDuckProviderUIConfig blackDuckProviderUIConfig = new BlackDuckProviderUIConfig(blackDuckProviderKey, encryptionValidator, configurationAccessor);
        blackDuckProviderUIConfig.setConfigFields();
        BlackDuckGlobalConfigurationValidator blackDuckGlobalConfigurationValidator = new BlackDuckGlobalConfigurationValidator(configurationAccessor);
        BlackDuckDescriptor descriptor = new BlackDuckDescriptor(blackDuckProviderKey, blackDuckProviderUIConfig, blackDuckDistributionUIConfig, blackDuckGlobalConfigurationValidator);
        Set<DefinedFieldModel> fields = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        assertEquals(5, fields.size());

        fields = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        assertEquals(8, fields.size());
    }

}
