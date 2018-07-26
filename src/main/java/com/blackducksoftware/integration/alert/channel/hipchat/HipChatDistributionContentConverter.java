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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;

@Component
public class HipChatDistributionContentConverter extends DatabaseContentConverter {
    @Autowired
    public HipChatDistributionContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public Config getRestModelFromJson(final String json) {
        return getContentConverter().getJsonContent(json, HipChatDistributionConfig.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final Config restModel) {
        final HipChatDistributionConfig hipChatRestModel = (HipChatDistributionConfig) restModel;
        final Integer roomId = getContentConverter().getIntegerValue(hipChatRestModel.getRoomId());
        final HipChatDistributionConfigEntity hipChatEntity = new HipChatDistributionConfigEntity(roomId, hipChatRestModel.getNotify(), hipChatRestModel.getColor());
        addIdToEntityPK(hipChatRestModel.getId(), hipChatEntity);
        return hipChatEntity;
    }

    @Override
    public Config populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
        final HipChatDistributionConfig hipChatRestModel = new HipChatDistributionConfig();
        final String id = getContentConverter().getStringValue(hipChatEntity.getId());
        final String roomId = getContentConverter().getStringValue(hipChatEntity.getRoomId());
        hipChatRestModel.setDistributionConfigId(id);
        hipChatRestModel.setRoomId(roomId);
        hipChatRestModel.setNotify(hipChatEntity.getNotify());
        hipChatRestModel.setColor(hipChatEntity.getColor());
        return hipChatRestModel;
    }

}
