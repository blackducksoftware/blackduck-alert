/**
 * hub-alert
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global.GlobalHipChatConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class HipChatChannelPropertyInitializer extends AbstractPropertyInitializer<GlobalHipChatConfigEntity> {
    public static final String PROPERTY_PREFIX_CHANNEL_HIPCHAT = "CHANNEL_HIPCHAT";
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannelPropertyInitializer.class);
    private final GlobalHipChatRepository globalHipChatRepository;

    @Autowired
    public HipChatChannelPropertyInitializer(final GlobalHipChatRepository globalHipChatRepository) {
        this.globalHipChatRepository = globalHipChatRepository;
    }

    @Override
    public String getPropertyNamePrefix() {
        return PROPERTY_PREFIX_CHANNEL_HIPCHAT;
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public ConfigRestModel getRestModelInstance() {
        return new GlobalHipChatConfigRestModel();
    }

    @Override
    public void save(final DatabaseEntity entity) {
        logger.info("Saving HipChat channel global properties {}", entity);
        // ps - dislike that I have to do this at all but this is the only place where the check is made.
        if (entity instanceof GlobalHipChatConfigEntity) {
            final GlobalHipChatConfigEntity entityToSave = (GlobalHipChatConfigEntity) entity;
            final List<GlobalHipChatConfigEntity> savedEntityList = this.globalHipChatRepository.findAll();
            if (savedEntityList == null || savedEntityList.isEmpty()) {
                this.globalHipChatRepository.save(entityToSave);
            } else {
                savedEntityList.forEach(savedEntity -> {
                    updateEntityWithDefaults(savedEntity, entityToSave);
                    this.globalHipChatRepository.save(savedEntity);
                });

            }
        }
    }

    @Override
    public boolean canSetDefaultProperties() {
        return globalHipChatRepository.findAll().isEmpty();
    }
}
