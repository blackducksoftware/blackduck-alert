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
package com.blackducksoftware.integration.hub.alert.provider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ProviderDescriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.provider.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class HubProvider extends ProviderDescriptor {
    public static final String PROVIDER_NAME = "provider_hub";

    @Autowired
    public HubProvider() {
        super(PROVIDER_NAME);
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

    @Override
    public Class<? extends DatabaseEntity> getGlobalEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public Class<? extends ConfigRestModel> getGlobalRestModelClass() {
        return GlobalHubConfigRestModel.class;
    }

}
