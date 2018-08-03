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
package com.blackducksoftware.integration.alert.channel.slack.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.config.TypeConverter;
import com.blackducksoftware.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.SlackDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;

@Component
public class SlackDistributionTypeConverter extends TypeConverter {

    @Autowired
    public SlackDistributionTypeConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public Config getConfigFromJson(final String json) {
        return getContentConverter().getJsonContent(json, SlackDistributionConfig.class);
    }

    @Override
    public DatabaseEntity populateEntityFromConfig(final Config restModel) {
        final SlackDistributionConfig slackRestModel = (SlackDistributionConfig) restModel;
        final SlackDistributionConfigEntity slackEntity = new SlackDistributionConfigEntity(slackRestModel.getWebhook(), slackRestModel.getChannelUsername(), slackRestModel.getChannelName());
        addIdToEntityPK(slackRestModel.getId(), slackEntity);
        return slackEntity;
    }

    @Override
    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) entity;
        final SlackDistributionConfig slackRestModel = new SlackDistributionConfig();
        final String id = getContentConverter().getStringValue(slackEntity.getId());
        slackRestModel.setDistributionConfigId(id);
        slackRestModel.setWebhook(slackEntity.getWebhook());
        slackRestModel.setChannelUsername(slackEntity.getChannelUsername());
        slackRestModel.setChannelName(slackEntity.getChannelName());
        return slackRestModel;
    }

}
