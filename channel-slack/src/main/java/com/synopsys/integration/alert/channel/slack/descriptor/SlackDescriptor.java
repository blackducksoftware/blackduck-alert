/*
 * channel-slack
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class SlackDescriptor extends ChannelDescriptor {
    public static final String SLACK_PREFIX = "slack.";
    public static final String SLACK_CHANNEL_PREFIX = "channel." + SLACK_PREFIX;

    public static final String KEY_WEBHOOK = SLACK_CHANNEL_PREFIX + "webhook";
    public static final String KEY_CHANNEL_NAME = SLACK_CHANNEL_PREFIX + "channel.name";
    public static final String KEY_CHANNEL_USERNAME = SLACK_CHANNEL_PREFIX + "channel.username";

    public static final String SLACK_LABEL = "Slack";
    public static final String SLACK_URL = "slack";
    public static final String SLACK_DESCRIPTION = "Configure Slack for Alert.";

    @Autowired
    public SlackDescriptor(SlackUIConfig slackUIConfig, SlackGlobalUIConfig slackGlobalUIConfig) {
        super(ChannelKeys.SLACK, slackUIConfig, slackGlobalUIConfig);
    }

}
