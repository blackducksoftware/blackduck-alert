/**
 * alert-database
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;
import com.synopsys.integration.alert.common.persistence.accessor.BaseSettingsKeyAccessor;
import com.synopsys.integration.alert.database.settings.SettingsKeyEntity;
import com.synopsys.integration.alert.database.settings.SettingsKeyRepository;

@Component
@Transactional
public class SettingsKeyAccessor implements BaseSettingsKeyAccessor {
    private final SettingsKeyRepository settingsKeyRepository;

    public SettingsKeyAccessor(final SettingsKeyRepository settingsKeyRepository) {
        this.settingsKeyRepository = settingsKeyRepository;
    }

    @Override
    public List<SettingsKeyModel> getSettingsKeys() {
        return settingsKeyRepository.findAll()
                   .stream()
                   .map(this::convertToSettingsKeyModel)
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<SettingsKeyModel> getSettingsKeyByKey(final String key) {
        return settingsKeyRepository.findByKey(key).map(this::convertToSettingsKeyModel);
    }

    @Override
    public SettingsKeyModel saveSettingsKey(final String key, final String value) {
        final Optional<SettingsKeyEntity> settingsKeyOptional = settingsKeyRepository.findByKey(key);
        if (settingsKeyOptional.isPresent()) {
            final Long id = settingsKeyOptional.get().getId();
            final SettingsKeyEntity settingsKeyEntity = new SettingsKeyEntity(key, value);
            settingsKeyEntity.setId(id);
            final SettingsKeyEntity updatedSettingsKeyEntity = settingsKeyRepository.save(settingsKeyEntity);
            return convertToSettingsKeyModel(updatedSettingsKeyEntity);
        }

        final SettingsKeyEntity newSettingsKeyEntity = settingsKeyRepository.save(new SettingsKeyEntity(key, value));
        return convertToSettingsKeyModel(newSettingsKeyEntity);
    }

    @Override
    public void deleteSettingsKeyByKey(final String key) {
        getSettingsKeyByKey(key).ifPresent(settingsKeyModel -> deleteSettingsKeyById(settingsKeyModel.getId()));
    }

    @Override
    public void deleteSettingsKeyById(final Long id) {
        if (null != id) {
            settingsKeyRepository.deleteById(id);
        }
    }

    private SettingsKeyModel convertToSettingsKeyModel(final SettingsKeyEntity settingsKeyEntity) {
        return new SettingsKeyModel(settingsKeyEntity.getId(), settingsKeyEntity.getKey(), settingsKeyEntity.getValue());
    }
}
