package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackUIConfig;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;

public class ChannelDistributionUIConfigTest {

    @Test
    public void createCommonConfigFieldsTest() throws AlertDatabaseConstraintException {
        final DescriptorMap descriptorAccessor = Mockito.mock(DescriptorMap.class);
        final Descriptor descriptor = Mockito.mock(Descriptor.class);
        final DescriptorMetadata descriptorMetadata = new DescriptorMetadata("", "", "", "", DescriptorType.CHANNEL, ConfigContextEnum.DISTRIBUTION, "", false, List.of(), "");
        Mockito.when(descriptor.getMetaData(Mockito.any())).thenReturn(Optional.of(descriptorMetadata));
        Mockito.when(descriptorAccessor.getDescriptorByType(DescriptorType.CHANNEL)).thenReturn(Set.of(descriptor));

        final ChannelDistributionUIConfig channelDistributionUIConfig = new SlackUIConfig(descriptorAccessor);

        final List<ConfigField> commonConfigFields = channelDistributionUIConfig.createFields();
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
