/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution.SlackDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class SlackDescriptor implements ChannelDescriptor {
    private final SlackChannel slackChannel;
    private final SlackDistributionConfigActions slackDistributionConfigActions;
    private final SlackDistributionRepository slackDistributionRepository;

    @Autowired
    public SlackDescriptor(final SlackChannel slackChannel, final SlackDistributionConfigActions slackDistributionConfigActions, final SlackDistributionRepository slackDistributionRepository) {
        this.slackChannel = slackChannel;
        this.slackDistributionConfigActions = slackDistributionConfigActions;
        this.slackDistributionRepository = slackDistributionRepository;
    }

    @Override
    public String getName() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Override
    public String getDestinationName() {
        return SlackChannel.COMPONENT_NAME;
    }

    @Override
    public boolean hasGlobalConfiguration() {
        return false;
    }

    @Override
    public Class<SlackDistributionConfigEntity> getDistributionEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

    @Override
    public Class<GlobalSlackConfigEntity> getGlobalEntityClass() {
        return GlobalSlackConfigEntity.class;
    }

    @Override
    public <R extends ConfigRestModel> Class<R> getGlobalRestModelClass() {
        return null;
    }

    @Override
    public <R extends JpaRepository<DatabaseEntity, Long>> R getGlobalRepository() {
        return null;
    }

    @Override
    public DistributionChannel getChannelComponent() {
        return slackChannel;
    }

    @Override
    public SlackDistributionRepository getDistributionRepository() {
        return slackDistributionRepository;
    }

    @Override
    public SlackDistributionConfigActions getSimpleConfigActions() {
        return slackDistributionConfigActions;
    }

    @Override
    public Class<SlackDistributionRestModel> getDistributionRestModelClass() {
        return SlackDistributionRestModel.class;
    }

}
