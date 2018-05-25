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
package com.blackducksoftware.integration.hub.alert.channel.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractChannelPropertyInitializer;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;

@Component
public class EmailChannelPropertyInitializer extends AbstractChannelPropertyInitializer<GlobalEmailConfigEntity> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannelPropertyInitializer.class);
    private final GlobalEmailRepositoryWrapper globalEmailRepository;

    @Autowired
    public EmailChannelPropertyInitializer(final GlobalEmailRepositoryWrapper globalEmailRepository) {
        this.globalEmailRepository = globalEmailRepository;
    }

    @Override
    public GlobalEmailConfigRestModel getRestModelInstance() {
        return new GlobalEmailConfigRestModel();
    }

    @Override
    public boolean canSetDefaultProperties() {
        return globalEmailRepository.findAll().isEmpty();
    }

    @Override
    public Class<GlobalEmailConfigEntity> getEntityClass() {
        return GlobalEmailConfigEntity.class;
    }

    @Override
    public void save(final DatabaseEntity entity) {
        logger.info("Saving Email channel global properties {}", entity);
        // ps - dislike that I have to do this at all but this is the only place where the check is made.
        if (entity instanceof GlobalEmailConfigEntity) {
            final GlobalEmailConfigEntity entityToSave = (GlobalEmailConfigEntity) entity;
            this.globalEmailRepository.save(entityToSave);
        }
    }
}
