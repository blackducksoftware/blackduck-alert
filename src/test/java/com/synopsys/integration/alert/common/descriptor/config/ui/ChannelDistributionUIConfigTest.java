package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

public class ChannelDistributionUIConfigTest {
    @Test
    public void createCommonConfigFieldsTest() {
        SlackChannelKey slackChannelKey = new SlackChannelKey();
        ChannelDistributionUIConfig channelDistributionUIConfig = new SlackUIConfig(slackChannelKey);
        channelDistributionUIConfig.setConfigFields();

        List<ConfigField> commonConfigFields = channelDistributionUIConfig.getFields();
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
