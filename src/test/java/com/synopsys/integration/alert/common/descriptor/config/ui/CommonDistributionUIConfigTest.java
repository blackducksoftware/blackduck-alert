package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;

public class CommonDistributionUIConfigTest {

    @Test
    public void createCommonConfigFieldsTest() {
        final ConfigurationAccessor accessor = Mockito.mock(ConfigurationAccessor.class);
        final CommonDistributionUIConfig commonDistributionUIConfig = new CommonDistributionUIConfig(accessor);

        final List<ConfigField> commonConfigFields = commonDistributionUIConfig.createCommonConfigFields(Set.of("example channel"), Set.of("example provider"));
        assertContains(commonConfigFields, CommonDistributionUIConfig.KEY_NAME);
        assertContains(commonConfigFields, CommonDistributionUIConfig.KEY_CHANNEL_NAME);
        assertContains(commonConfigFields, CommonDistributionUIConfig.KEY_PROVIDER_NAME);
        assertContains(commonConfigFields, CommonDistributionUIConfig.KEY_FREQUENCY);
    }

    private void assertContains(final List<ConfigField> commonConfigFields, final String expectedKey) {
        assertTrue(commonConfigFields
                       .stream()
                       .map(ConfigField::getKey)
                       .anyMatch(expectedKey::equals)
        );
    }
}
