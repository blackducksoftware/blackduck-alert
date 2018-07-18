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
package com.blackducksoftware.integration.alert.provider.hub;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigRestModel;

@Component
public class HubContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public HubContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<HipChatGlobalConfigRestModel> restModel = contentConverter.getContent(json, HipChatGlobalConfigRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }
        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final GlobalHubConfigRestModel hubRestModel = (GlobalHubConfigRestModel) restModel;
        final Integer hubTimeout = contentConverter.getInteger(hubRestModel.getHubTimeout());
        final GlobalHubConfigEntity hubEntity = new GlobalHubConfigEntity(hubTimeout, hubRestModel.getHubApiKey());
        addIdToEntityPK(hubRestModel.getId(), hubEntity);
        return hubEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final GlobalHubConfigEntity hubEntity = (GlobalHubConfigEntity) entity;
        final GlobalHubConfigRestModel hubRestModel = new GlobalHubConfigRestModel();
        final String id = contentConverter.convertToString(hubEntity.getId());
        final String hubTimeout = contentConverter.convertToString(hubEntity.getHubTimeout());
        hubRestModel.setId(id);
        hubRestModel.setHubTimeout(hubTimeout);
        hubRestModel.setHubApiKeyIsSet(StringUtils.isNotBlank(hubEntity.getHubApiKey()));
        hubRestModel.setHubApiKey(hubEntity.getHubApiKey());
        return hubRestModel;
    }

}
