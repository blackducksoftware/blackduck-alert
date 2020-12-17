/**
 * channel
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
