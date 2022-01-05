/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.SettingsKeyAccessor;
import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;
import com.synopsys.integration.alert.database.settings.SettingsKeyEntity;
import com.synopsys.integration.alert.database.settings.SettingsKeyRepository;

@Component
@Transactional
public class DefaultSettingsKeyAccessor implements SettingsKeyAccessor {
    private final SettingsKeyRepository settingsKeyRepository;

    public DefaultSettingsKeyAccessor(SettingsKeyRepository settingsKeyRepository) {
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
    public Optional<SettingsKeyModel> getSettingsKeyByKey(String key) {
        return settingsKeyRepository.findByKey(key).map(this::convertToSettingsKeyModel);
    }

    @Override
    public SettingsKeyModel saveSettingsKey(String key, String value) {
        Optional<SettingsKeyEntity> settingsKeyOptional = settingsKeyRepository.findByKey(key);
        if (settingsKeyOptional.isPresent()) {
            Long id = settingsKeyOptional.get().getId();
            SettingsKeyEntity settingsKeyEntity = new SettingsKeyEntity(key, value);
            settingsKeyEntity.setId(id);
            SettingsKeyEntity updatedSettingsKeyEntity = settingsKeyRepository.save(settingsKeyEntity);
            return convertToSettingsKeyModel(updatedSettingsKeyEntity);
        }

        SettingsKeyEntity newSettingsKeyEntity = settingsKeyRepository.save(new SettingsKeyEntity(key, value));
        return convertToSettingsKeyModel(newSettingsKeyEntity);
    }

    @Override
    public void deleteSettingsKeyByKey(String key) {
        getSettingsKeyByKey(key).ifPresent(settingsKeyModel -> deleteSettingsKeyById(settingsKeyModel.getId()));
    }

    @Override
    public void deleteSettingsKeyById(Long id) {
        if (null != id) {
            settingsKeyRepository.deleteById(id);
        }
    }

    private SettingsKeyModel convertToSettingsKeyModel(SettingsKeyEntity settingsKeyEntity) {
        return new SettingsKeyModel(settingsKeyEntity.getId(), settingsKeyEntity.getKey(), settingsKeyEntity.getValue());
    }
}
