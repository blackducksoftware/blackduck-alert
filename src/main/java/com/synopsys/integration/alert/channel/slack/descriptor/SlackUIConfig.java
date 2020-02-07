/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.slack.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;

@Component
public class SlackUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WEBHOOK = "Webhook";
    private static final String LABEL_CHANNEL_NAME = "Channel Name";
    private static final String LABEL_CHANNEL_USERNAME = "Channel Username";

    private static final String SLACK_WEBHOOK_DESCRIPTION = "The Slack URL to receive alerts.";
    private static final String SLACK_CHANNEL_NAME_DESCRIPTION = "The name of the Slack channel.";
    private static final String SLACK_CHANNEL_USERNAME_DESCRIPTION = "The username to show as the message sender in the Slack channel.";

    @Autowired
    public SlackUIConfig(@Lazy final DescriptorMap descriptorMap) {
        super(SlackChannel.COMPONENT_NAME, SlackDescriptor.SLACK_LABEL, SlackDescriptor.SLACK_URL, SlackDescriptor.SLACK_ICON, descriptorMap);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField webhook = TextInputConfigField.createRequired(SlackDescriptor.KEY_WEBHOOK, LABEL_WEBHOOK, SLACK_WEBHOOK_DESCRIPTION);
        final ConfigField channelName = TextInputConfigField.createRequired(SlackDescriptor.KEY_CHANNEL_NAME, LABEL_CHANNEL_NAME, SLACK_CHANNEL_NAME_DESCRIPTION);
        final ConfigField channelUsername = TextInputConfigField.create(SlackDescriptor.KEY_CHANNEL_USERNAME, LABEL_CHANNEL_USERNAME, SLACK_CHANNEL_USERNAME_DESCRIPTION);
        return List.of(webhook, channelName, channelUsername);
    }
}