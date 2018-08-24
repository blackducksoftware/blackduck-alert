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

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;

@Component
public class SlackUIConfig extends UIConfig {

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Slack", "slack", SlackChannel.COMPONENT_NAME, "slack", setupFields());
    }

    public List<ConfigField> setupFields() {
        final ConfigField webhook = new TextInputConfigField("webhook", "Webhook", true, false);
        final ConfigField channelUsername = new TextInputConfigField("channelUsername", "Channel Username", false, false);
        final ConfigField channelName = new TextInputConfigField("channelName", "Channel Name", true, false);
        return Arrays.asList(webhook, channelUsername, channelName);
    }

}
