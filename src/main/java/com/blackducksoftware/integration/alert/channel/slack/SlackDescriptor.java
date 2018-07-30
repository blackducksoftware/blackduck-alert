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
package com.blackducksoftware.integration.alert.channel.slack;

import java.util.Map;
import java.util.Set;

import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.SlackDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class SlackDescriptor extends ChannelDescriptor {
    private final SlackChannel slackChannel;

    @Autowired
    public SlackDescriptor(final SlackChannel slackChannel, final SlackDistributionContentConverter slackDistributionContentConverter, final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor) {
        super(SlackChannel.COMPONENT_NAME, SlackChannel.COMPONENT_NAME, null, null, slackDistributionContentConverter, slackDistributionRepositoryAccessor);
        this.slackChannel = slackChannel;
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfig restModel, final Map<String, String> fieldErrors) {
        final SlackDistributionConfig slackRestModel = (SlackDistributionConfig) restModel;

        if (StringUtils.isBlank(slackRestModel.getWebhook())) {
            fieldErrors.put("webhook", "A webhook is required.");
        }
        if (StringUtils.isBlank(slackRestModel.getChannelName())) {
            fieldErrors.put("channelName", "A channel name is required.");
        }
    }

    @Override
    public MessageListener getChannelListener() {
        return slackChannel;
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfig restModel, final ChannelEvent event) throws IntegrationException {
        final SlackDistributionConfigEntity config = (SlackDistributionConfigEntity) getDistributionContentConverter().populateDatabaseEntityFromRestModel(restModel);
        slackChannel.sendAuditedMessage(event, config);
    }

    @Override
    public void validateGlobalConfig(final Config restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) {
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return null;
    }

    @Override
    public Config getGlobalRestModelObject() {
        return null;
    }

}
