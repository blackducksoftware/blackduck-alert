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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.ChannelDistributionDescriptorActionApi;

@Component
public class SlackDistributionDescriptorActionApi extends ChannelDistributionDescriptorActionApi {

    @Autowired
    public SlackDistributionDescriptorActionApi(final SlackChannel slackChannel, final BaseConfigurationAccessor configurationAccessor, final ContentConverter contentConverter, final List<ProviderDescriptor> providerDescriptors) {
        super(slackChannel, configurationAccessor, contentConverter, providerDescriptors);
    }

    @Override
    public void validateChannelConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String webhook = fieldAccessor.getString(SlackUIConfig.KEY_WEBHOOK).orElse(null);
        final String channelName = fieldAccessor.getString(SlackUIConfig.KEY_CHANNEL_NAME).orElse(null);
        if (StringUtils.isBlank(webhook)) {
            fieldErrors.put("webhook", "A webhook is required.");
        }
        if (StringUtils.isBlank(channelName)) {
            fieldErrors.put("channelName", "A channel name is required.");
        }
    }

}
