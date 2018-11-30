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
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigsEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldsEntity;
import com.synopsys.integration.alert.database.entity.descriptor.FieldValuesEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorsEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigsRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldsRepository;
import com.synopsys.integration.alert.database.repository.descriptor.FieldValuesRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorsRepository;

@Component
@Transactional
// FIXME make sure encryption / decryption is implemented
public class ConfigurationAccessor {
    private final RegisteredDescriptorsRepository registeredDescriptorsRepository;
    private final DescriptorFieldsRepository descriptorFieldsRepository;
    private final DescriptorConfigsRepository descriptorConfigsRepository;
    private final FieldValuesRepository fieldValuesRepository;

    @Autowired
    public ConfigurationAccessor(final RegisteredDescriptorsRepository registeredDescriptorsRepository, final DescriptorFieldsRepository descriptorFieldsRepository,
            final DescriptorConfigsRepository descriptorConfigsRepository, final FieldValuesRepository fieldValuesRepository) {
        this.registeredDescriptorsRepository = registeredDescriptorsRepository;
        this.descriptorFieldsRepository = descriptorFieldsRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.fieldValuesRepository = fieldValuesRepository;
    }

    public List<ConfigurationModel> getConfigurationsByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorsEntity> registeredDescriptorsEntity = registeredDescriptorsRepository.findFirstByName(descriptorName);
        if (registeredDescriptorsEntity.isPresent()) {
            return getConfigs(Collections.singleton(registeredDescriptorsEntity.get()));
        }
        return getConfigs(Collections.emptyList());
    }

    public List<ConfigurationModel> getConfigurationsByType(final String descriptorType) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorType)) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be empty");
        }
        final List<RegisteredDescriptorsEntity> registeredDescriptorsEntities = registeredDescriptorsRepository.findByType(descriptorType);
        return getConfigs(registeredDescriptorsEntities);
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
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final DescriptorConfigsEntity descriptorConfigToSave = new DescriptorConfigsEntity(descriptorId);
        final DescriptorConfigsEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        final ConfigurationModel createdConfig = new ConfigurationModel(descriptorId, savedDescriptorConfig.getId());
        if (configuredFields != null && !configuredFields.isEmpty()) {
            configuredFields.forEach(configuredField -> createdConfig.configuredFields.put(configuredField.getFieldKey(), configuredField));
        }
        return createdConfig;
    }

    /**
     * @return the config resulting from the update
     */
    public ConfigurationModel updateConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException {
        if (configModel == null) {
            throw new AlertDatabaseConstraintException("The config model cannot be null");
        }
        return updateConfiguration(configModel.getDescriptorConfigId(), configModel.configuredFields.values());
    }

    /**
     * @return the config after update
     */
    public ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        final Optional<DescriptorConfigsEntity> optionalDescriptorConfigsEntity = descriptorConfigsRepository.findById(descriptorConfigId);
        if (optionalDescriptorConfigsEntity.isPresent()) {
            final DescriptorConfigsEntity descriptorConfigsEntity = optionalDescriptorConfigsEntity.get();
            final List<FieldValuesEntity> oldValues = fieldValuesRepository.findByConfigId(descriptorConfigsEntity.getDescriptorId());
            fieldValuesRepository.deleteAll(oldValues);

            final ConfigurationModel updatedConfig = new ConfigurationModel(descriptorConfigsEntity.getDescriptorId(), descriptorConfigsEntity.getId());
            if (configuredFields != null && !configuredFields.isEmpty()) {
                for (final ConfigurationFieldModel configFieldModel : configuredFields) {
                    final Long fieldId = getFieldIdOrThrowException(descriptorConfigsEntity.getDescriptorId(), configFieldModel.getFieldKey());
                    for (final String value : configFieldModel.getFieldValues()) {
                        final FieldValuesEntity newFieldValue = new FieldValuesEntity(descriptorConfigId, fieldId, value);
                        fieldValuesRepository.save(newFieldValue);
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
        deleteConfiguration(configModel.getDescriptorConfigId());
    }

    public void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        descriptorConfigsRepository.deleteById(descriptorConfigId);
    }

    // TODO should we replace this with a JOIN?
    private List<ConfigurationModel> getConfigs(final Collection<RegisteredDescriptorsEntity> descriptors) throws AlertDatabaseConstraintException {
        final List<ConfigurationModel> configs = new ArrayList<>();
        for (final RegisteredDescriptorsEntity descriptorsEntity : descriptors) {
            final List<DescriptorConfigsEntity> descriptorConfigsEntities = descriptorConfigsRepository.findByDescriptorId(descriptorsEntity.getId());
            for (final DescriptorConfigsEntity descriptorConfigsEntity : descriptorConfigsEntities) {
                final ConfigurationModel newModel = new ConfigurationModel(descriptorsEntity.getId(), descriptorConfigsEntity.getId());
                final List<FieldValuesEntity> fieldValuesEntities = fieldValuesRepository.findByConfigId(descriptorConfigsEntity.getId());
                for (final FieldValuesEntity fieldValuesEntity : fieldValuesEntities) {
                    final DescriptorFieldsEntity descriptorFieldsEntity = descriptorFieldsRepository
                                                                                  .findById(fieldValuesEntity.getFieldId())
                                                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("Field id cannot be null"));
                    final String fieldKey = descriptorFieldsEntity.getKey();
                    final ConfigurationFieldModel fieldModel;
                    if (descriptorFieldsEntity.getSensitive()) {
                        fieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
                    } else {
                        fieldModel = ConfigurationFieldModel.create(fieldKey);
                    }
                    fieldModel.setFieldValue(fieldValuesEntity.getValue());
                    newModel.put(fieldModel);
                }
                configs.add(newModel);
            }
        }
        return configs;
    }

    private Long getDescriptorIdOrThrowException(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorsRepository
                       .findFirstByName(descriptorName)
                       .map(RegisteredDescriptorsEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor with the provided name was registered"));
    }

    private Long getFieldIdOrThrowException(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException {
        if (descriptorId == null) {
            throw new AlertDatabaseConstraintException("Descriptor id cannot be null");
        }
        if (StringUtils.isEmpty(fieldKey)) {
            throw new AlertDatabaseConstraintException("Field key cannot be empty");
        }
        return descriptorFieldsRepository
                       .findFirstByDescriptorIdAndKey(descriptorId, fieldKey)
                       .map(DescriptorFieldsEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("A field with that key did not exist"));
    }

    public class ConfigurationModel {
        private final Long registeredDescriptorId;
        private final Long descriptorConfigId;
        private final Map<String, ConfigurationFieldModel> configuredFields;

        private ConfigurationModel(final Long registeredDescriptorId, final Long descriptorConfigId) {
            this.registeredDescriptorId = registeredDescriptorId;
            this.descriptorConfigId = descriptorConfigId;
            this.configuredFields = new HashMap<>();
        }

        public Long getRegisteredDescriptorId() {
            return registeredDescriptorId;
        }

        public Long getDescriptorConfigId() {
            return descriptorConfigId;
        }

        public void put(final ConfigurationFieldModel configFieldModel) {
            Objects.requireNonNull(configFieldModel);
            final String fieldKey = configFieldModel.getFieldKey();
            Objects.requireNonNull(fieldKey);
            if (configuredFields.containsKey(fieldKey)) {
                final ConfigurationFieldModel oldConfigField = configuredFields.get(fieldKey);
                final List<String> values = combine(oldConfigField, configFieldModel);
                oldConfigField.setFieldValues(values);
            }
            configuredFields.put(fieldKey, configFieldModel);
        }

        /**
         * @return true if configFieldModel existed
         */
        public boolean delete(final ConfigurationFieldModel configFieldModel) {
            Objects.requireNonNull(configFieldModel);
            return delete(configFieldModel.getFieldKey());
        }

        /**
         * @return true if a ConfigurationFieldModel containing that fieldKey was present
         */
        public boolean delete(final String fieldKey) {
            Objects.requireNonNull(fieldKey);
            final boolean containsField = configuredFields.containsKey(fieldKey);
            configuredFields.remove(fieldKey);
            return containsField;
        }

        private List<String> combine(final ConfigurationFieldModel first, final ConfigurationFieldModel second) {
            return Stream.concat(first.getFieldValues().stream(), second.getFieldValues().stream()).collect(Collectors.toList());
        }
    }
}