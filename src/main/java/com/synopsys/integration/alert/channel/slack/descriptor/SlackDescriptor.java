/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;

@Component
public class SlackDescriptor extends ChannelDescriptor {
    public static final String KEY_WEBHOOK = "webhook";
    public static final String KEY_CHANNEL_NAME = "channel.name";
    public static final String KEY_CHANNEL_USERNAME = "channel.username";

    @Autowired
    public SlackDescriptor(final SlackChannel channelListener, final SlackDistributionDescriptorActionApi distributionRestApi, final SlackUIConfig slackUIConfig) {
        super(SlackChannel.COMPONENT_NAME, SlackChannel.COMPONENT_NAME, channelListener, distributionRestApi, slackUIConfig);
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.DISTRIBUTION == context) {
            final DefinedFieldModel webhook = DefinedFieldModel.createDistributionField(SlackDescriptor.KEY_WEBHOOK);
            final DefinedFieldModel channelUsername = DefinedFieldModel.createDistributionField(SlackDescriptor.KEY_CHANNEL_USERNAME);
            final DefinedFieldModel channelName = DefinedFieldModel.createDistributionField(SlackDescriptor.KEY_CHANNEL_NAME);
            return List.of(webhook, channelUsername, channelName);
        }
        return Collections.emptyList();
    }
}
