package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;

public class BlackDuckDescriptorTest {
    @Test
    public void testGetDefinedFields() {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        BlackDuckContent blackDuckContent = new BlackDuckContent();
        BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(blackDuckContent);
        BlackDuckProviderUIConfig blackDuckProviderUIConfig = new BlackDuckProviderUIConfig();
        BlackDuckDescriptor descriptor = new BlackDuckDescriptor(blackDuckProviderKey, blackDuckProviderUIConfig, blackDuckDistributionUIConfig);
        Set<DefinedFieldModel> fields = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        assertEquals(3, fields.size());

        fields = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        assertEquals(5, fields.size());
    }

}
