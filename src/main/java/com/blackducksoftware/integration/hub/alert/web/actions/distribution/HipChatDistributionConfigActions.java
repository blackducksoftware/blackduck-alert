/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;

@Component
public class HipChatDistributionConfigActions extends DistributionConfigActions<HipChatDistributionConfigEntity, HipChatDistributionRestModel> {
    private final HipChatChannel hipChatChannel;

    @Autowired
    public HipChatDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final ConfiguredProjectsRepository configuredProjectsRepository, final DistributionProjectRepository distributionProjectRepository,
            final JpaRepository<HipChatDistributionConfigEntity, Long> channelDistributionRepository, final ObjectTransformer objectTransformer, final HipChatChannel hipChatChannel) {
        super(HipChatDistributionConfigEntity.class, HipChatDistributionRestModel.class, commonDistributionRepository, configuredProjectsRepository, distributionProjectRepository, channelDistributionRepository, objectTransformer);
        this.hipChatChannel = hipChatChannel;
    }

    @Override
    public String channelTestConfig(final HipChatDistributionRestModel restModel) throws IntegrationException {
        final HipChatDistributionConfigEntity distributionConfig = objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatDistributionConfigEntity.class);
        return hipChatChannel.testMessage(distributionConfig);
    }

    @Override
    public HipChatDistributionRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final HipChatDistributionConfigEntity distributionEntity) throws AlertException {
        final HipChatDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, HipChatDistributionRestModel.class);
        restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
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
