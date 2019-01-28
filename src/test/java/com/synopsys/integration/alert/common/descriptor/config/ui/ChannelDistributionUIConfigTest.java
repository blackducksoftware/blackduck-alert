package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;

public class ChannelDistributionUIConfigTest {

    @Test
    public void createCommonConfigFieldsTest() {
        final ConfigurationAccessor accessor = Mockito.mock(ConfigurationAccessor.class);
        final ChannelDistributionUIConfig channelDistributionUIConfig = new ChannelDistributionUIConfig(accessor, descriptorAccessor);

        final List<ConfigField> commonConfigFields = channelDistributionUIConfig.createCommonConfigFields(Set.of("example channel"), Set.of("example provider"));
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_FREQUENCY);
    }

    private void assertContains(final List<ConfigField> commonConfigFields, final String expectedKey) {
        assertTrue(commonConfigFields
                       .stream()
                       .map(ConfigField::getKey)
                       .anyMatch(expectedKey::equals)
        );
    }
}
