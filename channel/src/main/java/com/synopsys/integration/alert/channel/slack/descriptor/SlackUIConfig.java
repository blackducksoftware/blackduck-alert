/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class SlackUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WEBHOOK = "Webhook";
    private static final String LABEL_CHANNEL_NAME = "Channel Name";
    private static final String LABEL_CHANNEL_USERNAME = "Channel Username";

    private static final String SLACK_WEBHOOK_DESCRIPTION = "The Slack URL to receive alerts.";
    private static final String SLACK_CHANNEL_NAME_DESCRIPTION = "The name of the Slack channel.";
    private static final String SLACK_CHANNEL_USERNAME_DESCRIPTION = "The username to show as the message sender in the Slack channel.";

    public SlackUIConfig() {
        super(ChannelKey.SLACK, SlackDescriptor.SLACK_LABEL, SlackDescriptor.SLACK_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField webhook = new URLInputConfigField(SlackDescriptor.KEY_WEBHOOK, LABEL_WEBHOOK, SLACK_WEBHOOK_DESCRIPTION).applyRequired(true);
        ConfigField channelName = new TextInputConfigField(SlackDescriptor.KEY_CHANNEL_NAME, LABEL_CHANNEL_NAME, SLACK_CHANNEL_NAME_DESCRIPTION).applyRequired(true);
        ConfigField channelUsername = new TextInputConfigField(SlackDescriptor.KEY_CHANNEL_USERNAME, LABEL_CHANNEL_USERNAME, SLACK_CHANNEL_USERNAME_DESCRIPTION);
        return List.of(webhook, channelName, channelUsername);
    }

}
