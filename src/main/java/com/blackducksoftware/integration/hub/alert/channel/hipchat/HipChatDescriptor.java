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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class HipChatDescriptor implements ChannelDescriptor {
    private final HipChatChannel hipChatChannel;
    private final GlobalHipChatRepository globalHipChatRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;
    private final HipChatDistributionConfigActions hipChatDistributionConfigActions;

    @Autowired
    public HipChatDescriptor(final GlobalHipChatRepository globalHipChatRepository, final HipChatChannel hipChatChannel, final HipChatDistributionRepository hipChatDistributionRepository,
            final HipChatDistributionConfigActions hipChatDistributionConfigActions) {
        this.globalHipChatRepository = globalHipChatRepository;
        this.hipChatChannel = hipChatChannel;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
        this.hipChatDistributionConfigActions = hipChatDistributionConfigActions;
    }

    @Override
    public String getName() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public String getDestinationName() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public boolean hasGlobalConfiguration() {
        return true;
    }

    @Override
    public Class<HipChatDistributionConfigEntity> getDistributionEntityClass() {
        return HipChatDistributionConfigEntity.class;
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getGlobalEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public Class<GlobalHipChatConfigRestModel> getGlobalRestModelClass() {
        return GlobalHipChatConfigRestModel.class;
    }

    @Override
    public GlobalHipChatRepository getGlobalRepository() {
        return globalHipChatRepository;
    }

    @Override
    public DistributionChannel getChannelComponent() {
        return hipChatChannel;
    }

    @Override
    public <R extends CommonDistributionConfigRestModel> Class<R> getDistributionRestModelClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HipChatDistributionRepository getDistributionRepository() {
        return hipChatDistributionRepository;
    }

    @Override
    public HipChatDistributionConfigActions getDistributionConfigActions() {
        return hipChatDistributionConfigActions;
    }

}
