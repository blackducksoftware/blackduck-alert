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
package com.blackducksoftware.integration.hub.alert.channel.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;

@Component
public class EmailGroupDescriptor implements ChannelDescriptor {
    private final EmailGroupChannel emailGroupChannel;
    private final GlobalEmailRepository globalEmailRepository;
    private final EmailGroupDistributionRepository emailGroupDistributionRepository;
    private final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions;

    @Autowired
    public EmailGroupDescriptor(final EmailGroupChannel emailGroupChannel, final GlobalEmailRepository globalEmailRepository, final EmailGroupDistributionRepository emailGroupDistributionRepository,
            final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions) {
        this.emailGroupChannel = emailGroupChannel;
        this.globalEmailRepository = globalEmailRepository;
        this.emailGroupDistributionRepository = emailGroupDistributionRepository;
        this.emailGroupDistributionConfigActions = emailGroupDistributionConfigActions;
    }

    @Override
    public String getName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public String getDestinationName() {
        return EmailGroupChannel.COMPONENT_NAME;
    }

    @Override
    public boolean hasGlobalConfiguration() {
        return true;
    }

    @Override
    public Class<EmailGroupDistributionConfigEntity> getDistributionEntityClass() {
        return EmailGroupDistributionConfigEntity.class;
    }

    @Override
    public Class<GlobalEmailConfigEntity> getGlobalEntityClass() {
        return GlobalEmailConfigEntity.class;
    }

    @Override
    public Class<GlobalEmailConfigRestModel> getGlobalRestModelClass() {
        return GlobalEmailConfigRestModel.class;
    }

    @Override
    public GlobalEmailRepository getGlobalRepository() {
        return globalEmailRepository;
    }

    @Override
    public DistributionChannel getChannelComponent() {
        return emailGroupChannel;
    }

    @Override
    public Class<EmailGroupDistributionRestModel> getDistributionRestModelClass() {
        return EmailGroupDistributionRestModel.class;
    }

    @Override
    public EmailGroupDistributionRepository getDistributionRepository() {
        return emailGroupDistributionRepository;
    }

    @Override
    public EmailGroupDistributionConfigActions getSimpleConfigActions() {
        return emailGroupDistributionConfigActions;
    }

}
