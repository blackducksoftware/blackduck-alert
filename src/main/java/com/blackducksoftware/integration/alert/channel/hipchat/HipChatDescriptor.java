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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatRepository;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.channel.model.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionRestModel;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.google.gson.Gson;

@Component
public class HipChatDescriptor extends ChannelDescriptor {
    private final HipChatChannel hipChatChannel;
    private final GlobalHipChatRepository globalHipChatRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;
    private final Gson gson;
    private final ObjectTransformer objectTransformer;

    @Autowired
    public HipChatDescriptor(final GlobalHipChatRepository globalHipChatRepository, final HipChatChannel hipChatChannel, final HipChatDistributionRepository hipChatDistributionRepository, final Gson gson,
            final ObjectTransformer objectTransformer) {
        super(HipChatChannel.COMPONENT_NAME, HipChatChannel.COMPONENT_NAME, true);
        this.globalHipChatRepository = globalHipChatRepository;
        this.hipChatChannel = hipChatChannel;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
        this.gson = gson;
        this.objectTransformer = objectTransformer;
    }

    @Override
    public List<? extends DatabaseEntity> readDistributionEntities() {
        return hipChatDistributionRepository.findAll();
    }

    @Override
    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        return hipChatDistributionRepository.findById(id);
    }

    @Override
    public Optional<? extends DatabaseEntity> saveDistributionEntity(final DatabaseEntity entity) {
        if (entity instanceof HipChatDistributionConfigEntity) {
            final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
            return Optional.of(hipChatDistributionRepository.save(hipChatEntity));
        }
        return Optional.empty();
    }

    @Override
    public void deleteDistributionEntity(final long id) {
        hipChatDistributionRepository.deleteById(id);
    }

    @Override
    public CommonDistributionConfigRestModel convertFromStringToDistributionRestModel(final String json) {
        return gson.fromJson(json, HipChatDistributionRestModel.class);
    }

    @Override
    public DatabaseEntity convertFromDistributionRestModelToDistributionConfigEntity(final CommonDistributionConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatDistributionConfigEntity.class);
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof HipChatDistributionRestModel) {
            final HipChatDistributionRestModel hipChatRestModel = (HipChatDistributionRestModel) restModel;
            if (StringUtils.isBlank(hipChatRestModel.getRoomId())) {
                fieldErrors.put("roomId", "A Room Id is required.");
            } else if (!StringUtils.isNumeric(hipChatRestModel.getRoomId())) {
                fieldErrors.put("roomId", "Room Id must be an integer value");
            }
        }
    }

    @Override
    public Optional<? extends CommonDistributionConfigRestModel> constructRestModel(final CommonDistributionConfigEntity commonEntity, final DatabaseEntity distributionEntity) throws AlertException {
        if (distributionEntity instanceof HipChatDistributionConfigEntity) {
            final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) distributionEntity;
            final HipChatDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, HipChatDistributionRestModel.class);
            restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
            restModel.setColor(hipChatEntity.getColor());
            restModel.setNotify(hipChatEntity.getNotify());
            restModel.setRoomId(String.valueOf(hipChatEntity.getRoomId()));
            return Optional.ofNullable(restModel);
        }
        return Optional.empty();
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) convertFromDistributionRestModelToDistributionConfigEntity(restModel);
        hipChatChannel.sendAuditedMessage(event, hipChatEntity);
    }

    @Override
    public MessageListener getChannelListener() {
        return hipChatChannel;
    }

    @Override
    public List<? extends DatabaseEntity> readGlobalEntities() {
        return globalHipChatRepository.findAll();
    }

    @Override
    public Optional<? extends DatabaseEntity> readGlobalEntity(final long id) {
        return globalHipChatRepository.findById(id);
    }

    @Override
    public Optional<? extends DatabaseEntity> saveGlobalEntity(final DatabaseEntity entity) {
        if (entity instanceof GlobalHipChatConfigEntity) {
            final GlobalHipChatConfigEntity hipChatEntity = (GlobalHipChatConfigEntity) entity;
            return Optional.ofNullable(globalHipChatRepository.save(hipChatEntity));
        }
        return Optional.empty();
    }

    @Override
    public void deleteGlobalEntity(final long id) {
        globalHipChatRepository.deleteById(id);
    }

    @Override
    public ConfigRestModel convertFromStringToGlobalRestModel(final String json) {
        return gson.fromJson(json, GlobalHipChatConfigRestModel.class);
    }

    @Override
    public DatabaseEntity convertFromGlobalRestModelToGlobalConfigEntity(final ConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, GlobalHipChatConfigEntity.class);
    }

    @Override
    public ConfigRestModel convertFromGlobalEntityToGlobalRestModel(final DatabaseEntity entity) throws AlertException {
        return objectTransformer.databaseEntityToConfigRestModel(entity, GlobalHipChatConfigRestModel.class);
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof GlobalHipChatConfigRestModel) {
            final GlobalHipChatConfigRestModel hipChatRestModel = (GlobalHipChatConfigRestModel) restModel;
            if (StringUtils.isBlank(hipChatRestModel.getApiKey())) {
                fieldErrors.put("apiKey", "ApiKey can't be blank");
            }
        }
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) throws IntegrationException {
        if (entity instanceof GlobalHipChatConfigEntity) {
            final GlobalHipChatConfigEntity hipChatEntity = (GlobalHipChatConfigEntity) entity;
            hipChatChannel.testGlobalConfig(hipChatEntity);
        } else {
            throw new AlertException("Error: Unexpected entity passed through.");
        }
    }

    @Override
    public Field[] getGlobalEntityFields() {
        return GlobalHipChatConfigEntity.class.getDeclaredFields();
    }

    @Override
    public ConfigRestModel getGlobalRestModelObject() {
        return new GlobalHipChatConfigRestModel();
    }

}
