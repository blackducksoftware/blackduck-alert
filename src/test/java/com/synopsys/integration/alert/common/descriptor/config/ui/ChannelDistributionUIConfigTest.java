package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.GlobalConfigExistsValidator;

public class ChannelDistributionUIConfigTest {
    @Test
    public void createCommonConfigFieldsTest() {
        SlackChannelKey slackChannelKey = new SlackChannelKey();
        ChannelDistributionUIConfig channelDistributionUIConfig = new SlackUIConfig(slackChannelKey, Mockito.mock(GlobalConfigExistsValidator.class));

        List<ConfigField> commonConfigFields = channelDistributionUIConfig.createFields();
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        assertContains(commonConfigFields, ChannelDistributionUIConfig.KEY_FREQUENCY);
    }

    private void assertContains(List<ConfigField> commonConfigFields, String expectedKey) {
        assertTrue(commonConfigFields
                       .stream()
                       .map(ConfigField::getKey)
                       .anyMatch(expectedKey::equals)
        );
    }

}
