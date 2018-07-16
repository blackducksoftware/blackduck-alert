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
package com.blackducksoftware.integration.alert.channel.hipchat;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class HipChatGlobalContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public HipChatGlobalContentConverter(final ContentConverter contentConverter) {
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
        final HipChatGlobalConfigRestModel hipChatRestModel = (HipChatGlobalConfigRestModel) restModel;
        final HipChatGlobalConfigEntity hipChatEntity = new HipChatGlobalConfigEntity(hipChatRestModel.getApiKey(), hipChatRestModel.getHostServer());
        return hipChatEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final HipChatGlobalConfigEntity hipChatEntity = (HipChatGlobalConfigEntity) entity;
        final String id = String.valueOf(hipChatEntity.getId());
        final boolean isApiKeySet = StringUtils.isNotBlank(hipChatEntity.getApiKey());
        final HipChatGlobalConfigRestModel hipChatRestModel = new HipChatGlobalConfigRestModel(id, hipChatEntity.getApiKey(), isApiKeySet, hipChatEntity.getHostServer());
        return hipChatRestModel;
    }

}
