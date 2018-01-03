/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

@Component
public class SlackDistributionConfigActions extends DistributionConfigActions<SlackDistributionConfigEntity, SlackDistributionRestModel> {
    private final SlackManager slackManager;

    @Autowired
    public SlackDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final SlackDistributionRepository repository, final ConfiguredProjectsActions<SlackDistributionRestModel> configuredProjectsActions,
            final NotificationTypesActions<SlackDistributionRestModel> notificationTypesActions, final ObjectTransformer objectTransformer, final SlackManager slackManager) {
        super(SlackDistributionConfigEntity.class, SlackDistributionRestModel.class, commonDistributionRepository, repository, configuredProjectsActions, notificationTypesActions, objectTransformer);
        this.slackManager = slackManager;
    }

    @Override
    public String channelTestConfig(final SlackDistributionRestModel restModel) throws IntegrationException {
        return slackManager.sendTestMessage(restModel);
    }

    @Override
    public SlackDistributionRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final SlackDistributionConfigEntity distributionEntity) throws AlertException {
        final SlackDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, SlackDistributionRestModel.class);
        restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
        restModel.setChannelName(distributionEntity.getChannelName());
        restModel.setChannelUsername(distributionEntity.getChannelUsername());
        restModel.setWebhook(distributionEntity.getWebhook());
        return restModel;
    }

    @Override
    public String getDistributionName() {
        return SupportedChannels.SLACK;
    }

}
