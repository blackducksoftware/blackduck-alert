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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatManager;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.DistributionConfigActions;

@Component
public class HipChatDistributionConfigActions extends DistributionConfigActions<HipChatDistributionConfigEntity, HipChatDistributionRestModel, HipChatDistributionRepositoryWrapper> {
    private final HipChatManager hipChatManager;

    @Autowired
    public HipChatDistributionConfigActions(final CommonDistributionRepositoryWrapper commonDistributionRepository, final HipChatDistributionRepositoryWrapper channelDistributionRepository,
            final ConfiguredProjectsActions<HipChatDistributionRestModel> configuredProjectsActions, final NotificationTypesActions<HipChatDistributionRestModel> notificationTypesActions, final ObjectTransformer objectTransformer,
            final HipChatManager hipChatManager) {
        super(HipChatDistributionConfigEntity.class, HipChatDistributionRestModel.class, commonDistributionRepository, channelDistributionRepository, configuredProjectsActions, notificationTypesActions, objectTransformer);
        this.hipChatManager = hipChatManager;
    }

    @Override
    public String channelTestConfig(final HipChatDistributionRestModel restModel) throws IntegrationException {
        return hipChatManager.sendTestMessage(restModel);
    }

    @Override
    public HipChatDistributionRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final HipChatDistributionConfigEntity distributionEntity) throws AlertException {
        final HipChatDistributionRestModel restModel = getObjectTransformer().databaseEntityToConfigRestModel(commonEntity, HipChatDistributionRestModel.class);
        restModel.setId(getObjectTransformer().objectToString(commonEntity.getId()));
        restModel.setColor(distributionEntity.getColor());
        restModel.setNotify(String.valueOf(distributionEntity.getNotify()));
        restModel.setRoomId(String.valueOf(distributionEntity.getRoomId()));
        return restModel;
    }

    @Override
    public String getDistributionName() {
        return SupportedChannels.HIPCHAT;
    }

}
