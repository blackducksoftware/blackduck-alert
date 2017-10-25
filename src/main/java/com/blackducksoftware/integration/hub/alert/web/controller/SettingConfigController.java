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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.SettingEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.SettingRepository;
import com.blackducksoftware.integration.hub.alert.web.model.SettingConfigRestModel;

@RestController
public class SettingConfigController implements ConfigController<SettingEntity, SettingConfigRestModel> {
    private final SettingRepository settingRepository;

    @Autowired
    public SettingConfigController(final SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    @GetMapping(value = "/setting")
    public List<SettingConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        if (id != null) {
            final SettingEntity foundEntity = settingRepository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(databaseModelToRestModel(foundEntity));
            } else {
                return Collections.emptyList();
            }
        }
        return databaseModelsToRestModels(settingRepository.findAll());
    }

    @Override
    public ResponseEntity<String> postConfig(final SettingConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> putConfig(final SettingConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> deleteConfig(final SettingConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<String> testConfig(final SettingConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SettingEntity restModelToDatabaseModel(final SettingConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SettingConfigRestModel databaseModelToRestModel(final SettingEntity databaseModel) {
        return new SettingConfigRestModel(databaseModel.getSettingName(), databaseModel.getSettingValue(), databaseModel.getSettingType());
    }

    @Override
    public List<SettingConfigRestModel> databaseModelsToRestModels(final List<SettingEntity> databaseModels) {
        final ArrayList<SettingConfigRestModel> restModels = new ArrayList<>();
        for (final SettingEntity settingConfig : databaseModels) {
            restModels.add(databaseModelToRestModel(settingConfig));
        }
        return restModels;
    }
}
