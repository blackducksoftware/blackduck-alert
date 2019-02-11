package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackUIConfig;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;

public class ChannelDistributionUIConfigTest {

    @Test
    public void createCommonConfigFieldsTest() throws AlertDatabaseConstraintException {
        final BaseDescriptorAccessor descriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        final RegisteredDescriptorModel registeredDescriptorModel = Mockito.mock(RegisteredDescriptorModel.class);
        Mockito.when(registeredDescriptorModel.getName()).thenReturn("example channel");
        Mockito.when(descriptorAccessor.getRegisteredDescriptorsByType(DescriptorType.CHANNEL)).thenReturn(List.of(registeredDescriptorModel));

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
