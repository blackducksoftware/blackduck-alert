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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Component
public class HipChatDescriptor extends ChannelDescriptor {
    private final HipChatChannel hipChatChannel;
    private final GlobalHipChatRepository globalHipChatRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;

    @Autowired
    public HipChatDescriptor(final GlobalHipChatRepository globalHipChatRepository, final HipChatChannel hipChatChannel, final HipChatDistributionRepository hipChatDistributionRepository) {
        super(HipChatChannel.COMPONENT_NAME, HipChatChannel.COMPONENT_NAME, true);
        this.globalHipChatRepository = globalHipChatRepository;
        this.hipChatChannel = hipChatChannel;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
    }

    @Override
    public List<? extends DatabaseEntity> readDistributionEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> saveDistributionEntity(final DatabaseEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteDistributionEntity(final long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public CommonDistributionConfigRestModel convertFromStringToDistributionRestModel(final String json) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseEntity convertFromDistributionRestModelToDistributionConfigEntity(final CommonDistributionConfigRestModel restModel) throws AlertException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<? extends CommonDistributionConfigRestModel> constructRestModel(final CommonDistributionConfigEntity commonEntity, final DatabaseEntity distributionEntity) throws AlertException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
        // TODO Auto-generated method stub

    }

    @Override
    public MessageListener getChannelListener() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends DatabaseEntity> getGlobalEntityClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends ConfigRestModel> getGlobalRestModelClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends DatabaseEntity> readGlobalEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> readGlobalEntity(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends DatabaseEntity> saveGlobalEntity(final DatabaseEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteGlobalEntity(final long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public ConfigRestModel convertFromStringToGlobalRestModel(final String json) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseEntity convertFromGlobalRestModelToGlobalConfigEntity(final ConfigRestModel restModel) throws AlertException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigRestModel convertFromGlobalEntityToGlobalRestModel(final DatabaseEntity entity) throws AlertException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) throws IntegrationException {
        // TODO Auto-generated method stub

    }

}
