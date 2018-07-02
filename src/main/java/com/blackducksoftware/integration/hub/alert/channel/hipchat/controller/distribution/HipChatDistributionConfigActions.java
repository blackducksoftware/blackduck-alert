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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.SimpleDistributionConfigActions;

@Component
public class HipChatDistributionConfigActions implements SimpleDistributionConfigActions<HipChatDistributionConfigEntity, HipChatDistributionRestModel> {
    private final ObjectTransformer objectTransformer;

    @Autowired
    public HipChatDistributionConfigActions(final ObjectTransformer objectTransformer) {
        this.objectTransformer = objectTransformer;
    }

    @Override
    public HipChatDistributionRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final HipChatDistributionConfigEntity distributionEntity) throws AlertException {
        final HipChatDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, HipChatDistributionRestModel.class);
        restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
        restModel.setColor(distributionEntity.getColor());
        restModel.setNotify(distributionEntity.getNotify());
        restModel.setRoomId(String.valueOf(distributionEntity.getRoomId()));
        return restModel;
    }

    @Override
    public void validateConfig(final HipChatDistributionRestModel restModel, final Map<String, String> fieldErrors) {
        if (StringUtils.isBlank(restModel.getRoomId())) {
            fieldErrors.put("roomId", "A Room Id is required.");
        } else if (!StringUtils.isNumeric(restModel.getRoomId())) {
            fieldErrors.put("roomId", "Room Id must be an integer value");
        }
    }

}
