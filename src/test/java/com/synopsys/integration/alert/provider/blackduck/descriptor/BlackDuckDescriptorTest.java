package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;

public class BlackDuckDescriptorTest {
    @Test
    public void testGetDefinedFields() {
        EncryptionSettingsValidator encryptionValidator = Mockito.mock(EncryptionSettingsValidator.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(encryptionValidator.apply(Mockito.any(), Mockito.any())).thenReturn(List.of());
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(blackDuckContent);
        BlackDuckProviderUIConfig blackDuckProviderUIConfig = new BlackDuckProviderUIConfig(blackDuckProviderKey, encryptionValidator, configurationAccessor);
        BlackDuckDescriptor descriptor = new BlackDuckDescriptor(blackDuckProviderKey, blackDuckProviderUIConfig, blackDuckDistributionUIConfig);
        Set<DefinedFieldModel> fields = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        assertEquals(5, fields.size());

        fields = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        assertEquals(8, fields.size());
    }

}
