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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

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
    public SlackDescriptor(SlackChannelKey slackChannelKey, SlackUIConfig slackUIConfig, SlackGlobalUIConfig slackGlobalUIConfig) {
        super(slackChannelKey, slackUIConfig, slackGlobalUIConfig);
    }

}
