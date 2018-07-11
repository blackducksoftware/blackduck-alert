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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.google.gson.Gson;

@Component
public class SlackDescriptor extends ChannelDescriptor {
    private final SlackChannel slackChannel;
    private final SlackDistributionRepository slackDistributionRepository;
    private final Gson gson;
    private final ObjectTransformer objectTransformer;

    @Autowired
    public SlackDescriptor(final SlackChannel slackChannel, final SlackDistributionRepository slackDistributionRepository, final Gson gson, final ObjectTransformer objectTransformer) {
        super(SlackChannel.COMPONENT_NAME, SlackChannel.COMPONENT_NAME, false);
        this.slackChannel = slackChannel;
        this.slackDistributionRepository = slackDistributionRepository;
        this.gson = gson;
        this.objectTransformer = objectTransformer;
    }

    @Override
    public List<? extends DatabaseEntity> readDistributionEntities() {
        return slackDistributionRepository.findAll();
    }

    @Override
    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        return slackDistributionRepository.findById(id);
    }

    @Override
    public Optional<? extends DatabaseEntity> saveDistributionEntity(final DatabaseEntity entity) {
        if (entity instanceof SlackDistributionConfigEntity) {
            final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) entity;
            return Optional.of(slackDistributionRepository.save(slackEntity));
        }
        return Optional.empty();
    }

    @Override
    public void deleteDistributionEntity(final long id) {
        slackDistributionRepository.deleteById(id);
    }

    @Override
    public CommonDistributionConfigRestModel convertFromStringToDistributionRestModel(final String json) {
        return gson.fromJson(json, SlackDistributionRestModel.class);
    }

    @Override
    public DatabaseEntity convertFromDistributionRestModelToDistributionConfigEntity(final CommonDistributionConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, SlackDistributionConfigEntity.class);
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof SlackDistributionRestModel) {
            final SlackDistributionRestModel slackRestModel = (SlackDistributionRestModel) restModel;

            if (StringUtils.isBlank(slackRestModel.getWebhook())) {
                fieldErrors.put("webhook", "A webhook is required.");
            }
            if (StringUtils.isBlank(slackRestModel.getChannelName())) {
                fieldErrors.put("channelName", "A channel name is required.");
            }
        }
    }

    @Override
    public Optional<? extends CommonDistributionConfigRestModel> constructRestModel(final CommonDistributionConfigEntity commonEntity, final DatabaseEntity distributionEntity) throws AlertException {
        if (distributionEntity instanceof SlackDistributionConfigEntity) {
            final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) distributionEntity;
            final SlackDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, SlackDistributionRestModel.class);
            restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
            restModel.setChannelName(slackEntity.getChannelName());
            restModel.setChannelUsername(slackEntity.getChannelUsername());
            restModel.setWebhook(slackEntity.getWebhook());
            return Optional.ofNullable(restModel);
        }
        return Optional.empty();
    }

    @Override
    public MessageListener getChannelListener() {
        return slackChannel;
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
        final SlackDistributionConfigEntity config = (SlackDistributionConfigEntity) convertFromDistributionRestModelToDistributionConfigEntity(restModel);
        slackChannel.sendAuditedMessage(event, config);
    }

    @Override
    public List<? extends DatabaseEntity> readGlobalEntities() {
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> readGlobalEntity(final long id) {
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> saveGlobalEntity(final DatabaseEntity entity) {
        return null;
    }

    @Override
    public void deleteGlobalEntity(final long id) {
    }

    @Override
    public CommonDistributionConfigRestModel convertFromStringToGlobalRestModel(final String json) {
        return null;
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
    }

    @Override
    public DatabaseEntity convertFromGlobalRestModelToGlobalConfigEntity(final ConfigRestModel restModel) {
        return null;
    }

    @Override
    public ConfigRestModel convertFromGlobalEntityToGlobalRestModel(final DatabaseEntity entity) throws AlertException {
        return null;
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) {
    }

    @Override
    public Field[] getGlobalEntityFields() {
        return null;
    }

    @Override
    public ConfigRestModel getGlobalRestModelObject() {
        return null;
    }

}
