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
package com.synopsys.integration.alert.database.api.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldEntity;
import com.synopsys.integration.alert.database.entity.descriptor.FieldValueEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;
import com.synopsys.integration.util.Stringable;

@Component
@Transactional
public class ConfigurationAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorFieldRepository descriptorFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public ConfigurationAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DescriptorFieldRepository descriptorFieldRepository,
            final DescriptorConfigRepository descriptorConfigsRepository, final FieldValueRepository fieldValueRepository, final EncryptionUtility encryptionUtility) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorFieldRepository = descriptorFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
    }

    public Optional<ConfigurationModel> getConfigurationById(final Long id) throws AlertDatabaseConstraintException {
        if (id == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        final Optional<DescriptorConfigEntity> optionalDescriptorConfigEntity = descriptorConfigsRepository.findById(id);
        if (optionalDescriptorConfigEntity.isPresent()) {
            final DescriptorConfigEntity descriptorConfigEntity = optionalDescriptorConfigEntity.get();
            return Optional.of(createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId()));
        }
        return Optional.empty();
    }

    public List<ConfigurationModel> getConfigurationsByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (registeredDescriptorEntity.isPresent()) {
            return createConfigModels(Collections.singleton(registeredDescriptorEntity.get()));
        }
        return createConfigModels(Collections.emptyList());
    }

    public List<ConfigurationModel> getConfigurationsByType(final String descriptorType) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorType)) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be empty");
        }
        final List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByType(descriptorType);
        return createConfigModels(registeredDescriptorEntities);
    }

    /**
     * @return the config that was created
     */
    public ConfigurationModel createEmptyConfiguration(final String descriptorName) throws AlertDatabaseConstraintException {
        return createConfiguration(descriptorName, null);
    }

    /**
     * @return the config that was created
     */
    public ConfigurationModel createConfiguration(final String descriptorName, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final DescriptorConfigEntity descriptorConfigToSave = new DescriptorConfigEntity(descriptorId);
        final DescriptorConfigEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        final ConfigurationModel createdConfig = new ConfigurationModel(descriptorId, savedDescriptorConfig.getId());
        if (configuredFields != null && !configuredFields.isEmpty()) {
            for (final ConfigurationFieldModel configuredField : configuredFields) {
                if (configuredField.isSet()) {
                    final DescriptorFieldEntity associatedField = descriptorFieldRepository
                                                                          .findFirstByDescriptorIdAndKey(createdConfig.getDescriptorId(), configuredField.getFieldKey())
                                                                          .orElseThrow(() -> new AlertDatabaseConstraintException("The config is attempting to set a field not associated with its descriptor"));
                    for (final String value : configuredField.getFieldValues()) {
                        final FieldValueEntity newFieldValueEntity = new FieldValueEntity(createdConfig.getConfigurationId(), associatedField.getId(), encrypt(value, configuredField.isSensitive()));
                        fieldValueRepository.save(newFieldValueEntity);
                    }
                }
                createdConfig.configuredFields.put(configuredField.getFieldKey(), configuredField);
            }
        }
        return createdConfig;
    }

    /**
     * @return the config after update
     */
    public ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        final Optional<DescriptorConfigEntity> optionalDescriptorConfigEntity = descriptorConfigsRepository.findById(descriptorConfigId);
        if (optionalDescriptorConfigEntity.isPresent()) {
            final DescriptorConfigEntity descriptorConfigEntity = optionalDescriptorConfigEntity.get();
            final List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(descriptorConfigEntity.getDescriptorId());
            fieldValueRepository.deleteAll(oldValues);

            final ConfigurationModel updatedConfig = new ConfigurationModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId());
            if (configuredFields != null && !configuredFields.isEmpty()) {
                for (final ConfigurationFieldModel configFieldModel : configuredFields) {
                    final Long fieldId = getFieldIdOrThrowException(descriptorConfigEntity.getDescriptorId(), configFieldModel.getFieldKey());
                    for (final String value : configFieldModel.getFieldValues()) {
                        final FieldValueEntity newFieldValue = new FieldValueEntity(descriptorConfigId, fieldId, encrypt(value, configFieldModel.isSensitive()));
                        fieldValueRepository.save(newFieldValue);
                    }
                    updatedConfig.put(configFieldModel);
                }
            }
            return updatedConfig;
        }
        throw new AlertDatabaseConstraintException("A config with that id did not exist");
    }

    public void deleteConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException {
        if (configModel == null) {
            throw new AlertDatabaseConstraintException("Cannot delete a null object from the database");
        }
        deleteConfiguration(configModel.getConfigurationId());
    }

    public void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        descriptorConfigsRepository.deleteById(descriptorConfigId);
    }

    private List<ConfigurationModel> createConfigModels(final Collection<RegisteredDescriptorEntity> descriptors) throws AlertDatabaseConstraintException {
        final List<ConfigurationModel> configs = new ArrayList<>();
        for (final RegisteredDescriptorEntity descriptorEntity : descriptors) {
            final List<DescriptorConfigEntity> descriptorConfigEntities = descriptorConfigsRepository.findByDescriptorId(descriptorEntity.getId());
            for (final DescriptorConfigEntity descriptorConfigEntity : descriptorConfigEntities) {
                final ConfigurationModel newModel = createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId());
                configs.add(newModel);
            }
        }
        return configs;
    }

    private ConfigurationModel createConfigModel(final Long descriptorId, final Long configId) throws AlertDatabaseConstraintException {
        final ConfigurationModel newModel = new ConfigurationModel(descriptorId, configId);
        final List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (final FieldValueEntity fieldValueEntity : fieldValueEntities) {
            final DescriptorFieldEntity descriptorFieldEntity = descriptorFieldRepository
                                                                        .findById(fieldValueEntity.getFieldId())
                                                                        .orElseThrow(() -> new AlertDatabaseConstraintException("Field id cannot be null"));
            final String fieldKey = descriptorFieldEntity.getKey();
            final ConfigurationFieldModel fieldModel;
            if (descriptorFieldEntity.getSensitive()) {
                fieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
            } else {
                fieldModel = ConfigurationFieldModel.create(fieldKey);
            }
            final String decryptedValue = decrypt(fieldValueEntity.getValue(), fieldModel.isSensitive());
            fieldModel.setFieldValue(decryptedValue);
            newModel.put(fieldModel);
        }
        return newModel;
    }

    private Long getDescriptorIdOrThrowException(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorRepository
                       .findFirstByName(descriptorName)
                       .map(RegisteredDescriptorEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor with the provided name was registered"));
    }

    private Long getFieldIdOrThrowException(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException {
        if (descriptorId == null) {
            throw new AlertDatabaseConstraintException("Descriptor id cannot be null");
        }
        if (StringUtils.isEmpty(fieldKey)) {
            throw new AlertDatabaseConstraintException("Field key cannot be empty");
        }
        return descriptorFieldRepository
                       .findFirstByDescriptorIdAndKey(descriptorId, fieldKey)
                       .map(DescriptorFieldEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("A field with that key did not exist"));
    }

    private String encrypt(final String value, final Boolean shouldEncrypt) {
        if (shouldEncrypt && value != null) {
            return encryptionUtility.encrypt(value);
        }
        return value;
    }

    private String decrypt(final String value, final Boolean shouldDecrypt) {
        if (shouldDecrypt && value != null) {
            return encryptionUtility.decrypt(value);
        }
        return value;
    }

    public final class ConfigurationModel extends Stringable {
        private final Long descriptorId;
        private final Long configurationId;
        private final Map<String, ConfigurationFieldModel> configuredFields;

        private ConfigurationModel(final Long registeredDescriptorId, final Long descriptorConfigId) {
            this.descriptorId = registeredDescriptorId;
            this.configurationId = descriptorConfigId;
            this.configuredFields = new HashMap<>();
        }

        public Long getDescriptorId() {
            return descriptorId;
        }

        public Long getConfigurationId() {
            return configurationId;
        }

        public Optional<ConfigurationFieldModel> getField(final String fieldKey) {
            Objects.requireNonNull(fieldKey);
            return Optional.ofNullable(configuredFields.get(fieldKey));
        }

        public List<ConfigurationFieldModel> getCopyOfFieldList() {
            return new ArrayList<>(configuredFields.values());
        }

        public Map<String, ConfigurationFieldModel> getCopyOfKeyToFieldMap() {
            return new HashMap<>(configuredFields);
        }

        private void put(final ConfigurationFieldModel configFieldModel) {
            Objects.requireNonNull(configFieldModel);
            final String fieldKey = configFieldModel.getFieldKey();
            Objects.requireNonNull(fieldKey);
            if (configuredFields.containsKey(fieldKey)) {
                final ConfigurationFieldModel oldConfigField = configuredFields.get(fieldKey);
                final List<String> values = combine(oldConfigField, configFieldModel);
                oldConfigField.setFieldValues(values);
            } else {
                configuredFields.put(fieldKey, configFieldModel);
            }
        }

        private List<String> combine(final ConfigurationFieldModel first, final ConfigurationFieldModel second) {
            return Stream.concat(first.getFieldValues().stream(), second.getFieldValues().stream()).collect(Collectors.toList());
        }
    }
}