/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.web.model.FieldValueModel;

@Component
public class SlackUIConfig extends UIConfig {

    public SlackUIConfig() {
        super(SlackDescriptor.SLACK_LABEL, SlackDescriptor.SLACK_URL, SlackDescriptor.SLACK_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField webhook = TextInputConfigField.createRequired(SlackDescriptor.KEY_WEBHOOK, "Webhook", this::validateWebHook);
        final ConfigField channelName = TextInputConfigField.createRequired(SlackDescriptor.KEY_CHANNEL_NAME, "Channel Name", this::validateChannelName);
        final ConfigField channelUsername = TextInputConfigField.create(SlackDescriptor.KEY_CHANNEL_USERNAME, "Channel Username");
        return List.of(webhook, channelName, channelUsername);
    }

    private Collection<String> validateWebHook(final FieldValueModel fieldValueModel) {
        final String webhook = fieldValueModel.getValue().orElse(null);
        if (StringUtils.isBlank(webhook)) {
            return List.of("A webhook is required.");
        }
        return List.of();
    }

    private Collection<String> validateChannelName(final FieldValueModel fieldValueModel) {
        final String channelName = fieldValueModel.getValue().orElse(null);
        if (StringUtils.isBlank(channelName)) {
            return List.of("A channel name is required.");
        }
        return List.of();
    }
}
