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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class SlackDistributionContentConverter extends DatabaseContentConverter {

    @Autowired
    public SlackDistributionContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<SlackDistributionRestModel> restModel = getContentConverter().getContent(json, SlackDistributionRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }

        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final SlackDistributionRestModel slackRestModel = (SlackDistributionRestModel) restModel;
        final SlackDistributionConfigEntity slackEntity = new SlackDistributionConfigEntity(slackRestModel.getWebhook(), slackRestModel.getChannelUsername(), slackRestModel.getChannelName());
        addIdToEntityPK(slackRestModel.getId(), slackEntity);
        return slackEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) entity;
        final SlackDistributionRestModel slackRestModel = new SlackDistributionRestModel();
        final String id = getContentConverter().getStringValue(slackEntity.getId());
        slackRestModel.setDistributionConfigId(id);
        slackRestModel.setWebhook(slackEntity.getWebhook());
        slackRestModel.setChannelUsername(slackEntity.getChannelUsername());
        slackRestModel.setChannelName(slackEntity.getChannelName());
        return slackRestModel;
    }

}
