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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;
import com.google.gson.Gson;

@Component
public class HipChatConfigActions extends ConfigActions<HipChatConfigEntity, HipChatConfigRestModel> {
    private final Gson gson;

    @Autowired
    public HipChatConfigActions(final HipChatRepository hipChatRepository, final ObjectTransformer objectTransformer, final Gson gson) {
        super(HipChatConfigEntity.class, HipChatConfigRestModel.class, hipChatRepository, objectTransformer);
        this.gson = gson;
    }

    @Override
    public String validateConfig(final HipChatConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getRoomId()) && !StringUtils.isNumeric(restModel.getRoomId())) {
            fieldErrors.put("roomId", "Not an Integer.");
        }

        if (StringUtils.isNotBlank(restModel.getNotify()) && isBoolean(restModel.getNotify())) {
            fieldErrors.put("notify", "Not an Boolean.");
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final HipChatConfigRestModel restModel) throws IntegrationException {
        final HipChatChannel channel = new HipChatChannel(gson, (HipChatRepository) repository);
        return channel.testMessage(objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatConfigEntity.class));
    }

}
