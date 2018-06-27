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

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;

@Component
public class HipChatDescriptor implements ChannelDescriptor {
    private final HipChatChannel hipChatChannel;
    private final GlobalHipChatRepository globalHipChatRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;
    private final HipChatDistributionConfigActions hipChatDistributionConfigActions;
    private final GlobalHipChatConfigActions globalHipChatConfigActions;

    @Autowired
    public HipChatDescriptor(final GlobalHipChatRepository globalHipChatRepository, final HipChatChannel hipChatChannel, final HipChatDistributionRepository hipChatDistributionRepository,
            final HipChatDistributionConfigActions hipChatDistributionConfigActions, final GlobalHipChatConfigActions globalHipChatConfigActions) {
        this.globalHipChatRepository = globalHipChatRepository;
        this.hipChatChannel = hipChatChannel;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
        this.hipChatDistributionConfigActions = hipChatDistributionConfigActions;
        this.globalHipChatConfigActions = globalHipChatConfigActions;
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
    public Class<HipChatDistributionRestModel> getDistributionRestModelClass() {
        return HipChatDistributionRestModel.class;
    }

    @Override
    public HipChatDistributionRepository getDistributionRepository() {
        return hipChatDistributionRepository;
    }

    @Override
    public HipChatDistributionConfigActions getDistributionConfigActions() {
        return hipChatDistributionConfigActions;
    }

    @Override
    public GlobalHipChatConfigActions getGlobalConfigActions() {
        return globalHipChatConfigActions;
    }

}
