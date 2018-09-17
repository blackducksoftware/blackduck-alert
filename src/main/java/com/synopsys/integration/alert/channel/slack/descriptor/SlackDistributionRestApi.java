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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannelEvent;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SlackDistributionRestApi extends RestApi {
    private final ChannelEventFactory channelEventFactory;
    private final SlackChannel slackChannel;

    @Autowired
    public SlackDistributionRestApi(final SlackDistributionTypeConverter databaseContentConverter, final SlackDistributionRepositoryAccessor repositoryAccessor, final ChannelEventFactory channelEventFactory,
        final SlackChannel slackChannel) {
        super(databaseContentConverter, repositoryAccessor);
        this.channelEventFactory = channelEventFactory;
        this.slackChannel = slackChannel;
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final SlackDistributionConfig slackRestModel = (SlackDistributionConfig) restModel;

        if (StringUtils.isBlank(slackRestModel.getWebhook())) {
            fieldErrors.put("webhook", "A webhook is required.");
        }
        if (StringUtils.isBlank(slackRestModel.getChannelName())) {
            fieldErrors.put("channelName", "A channel name is required.");
        }
    }

    @Override
    public void testConfig(final Config restModel) throws IntegrationException {
        final SlackChannelEvent event = channelEventFactory.createSlackChannelTestEvent(restModel);
        slackChannel.sendAuditedMessage(event);
    }

}
