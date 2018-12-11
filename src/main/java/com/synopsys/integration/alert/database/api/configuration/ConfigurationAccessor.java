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
package com.synopsys.integration.alert.database.api.configuration;

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

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.util.Stringable;

@Component
@Transactional
public class ConfigurationAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public ConfigurationAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DefinedFieldRepository definedFieldRepository,
        final DescriptorConfigRepository descriptorConfigsRepository, final ConfigContextRepository configContextRepository, final FieldValueRepository fieldValueRepository, final EncryptionUtility encryptionUtility) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configContextRepository = configContextRepository;
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
            return Optional.of(createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(), descriptorConfigEntity.getContextId()));
        }
        return Optional.empty();
    }

    public List<ConfigurationModel> getConfigurationsByDescriptorName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (registeredDescriptorEntity.isPresent()) {
            return createConfigModels(Collections.singleton(registeredDescriptorEntity.get()));
        }
        return createConfigModels(Collections.emptyList());
    }

    /**
     * @return the config that was created
     */
    public ConfigurationModel createEmptyConfiguration(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createConfiguration(descriptorName, context, null);
    }

    /**
     * @return the config that was created
     */
    public ConfigurationModel createConfiguration(final String descriptorName, final ConfigContextEnum context, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final Long configContextId = getConfigContextIdOrThrowException(context);
        final DescriptorConfigEntity descriptorConfigToSave = new DescriptorConfigEntity(descriptorId, configContextId);
        final DescriptorConfigEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        final ConfigurationModel createdConfig = new ConfigurationModel(descriptorId, savedDescriptorConfig.getId(), context);
        if (configuredFields != null && !configuredFields.isEmpty()) {
            for (final ConfigurationFieldModel configuredField : configuredFields) {
                final String fieldKey = configuredField.getFieldKey();
                if (configuredField.isSet()) {
                    final DefinedFieldEntity associatedField = definedFieldRepository
                                                                   .findFirstByKey(fieldKey)
                                                                   .orElseThrow(() -> new AlertDatabaseConstraintException("A field with the provided key did not exist: " + fieldKey));
                    for (final String value : configuredField.getFieldValues()) {
                        final FieldValueEntity newFieldValueEntity = new FieldValueEntity(createdConfig.getConfigurationId(), associatedField.getId(), encrypt(value, configuredField.isSensitive()));
                        fieldValueRepository.save(newFieldValueEntity);
                    }
                }
                createdConfig.configuredFields.put(fieldKey, configuredField);
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
        final DescriptorConfigEntity descriptorConfigEntity = descriptorConfigsRepository
                                                                  .findById(descriptorConfigId)
                                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("A config with that id did not exist"));
        final List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(descriptorConfigEntity.getDescriptorId());
        fieldValueRepository.deleteAll(oldValues);

        final String configContext = getContextById(descriptorConfigEntity.getContextId());
        final ConfigurationModel updatedConfig = new ConfigurationModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(), configContext);
        if (configuredFields != null && !configuredFields.isEmpty()) {
            for (final ConfigurationFieldModel configFieldModel : configuredFields) {
                final Long fieldId = getFieldIdOrThrowException(configFieldModel.getFieldKey());
                for (final String value : configFieldModel.getFieldValues()) {
                    final FieldValueEntity newFieldValue = new FieldValueEntity(descriptorConfigId, fieldId, encrypt(value, configFieldModel.isSensitive()));
                    fieldValueRepository.save(newFieldValue);
                }
                updatedConfig.put(configFieldModel);
            }
        }
        return updatedConfig;
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
                final ConfigurationModel newModel = createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(), descriptorConfigEntity.getContextId());
                configs.add(newModel);
            }
        }
        return configs;
    }

    private ConfigurationModel createConfigModel(final Long descriptorId, final Long configId, final Long contextId) throws AlertDatabaseConstraintException {
        final String configContext = getContextById(contextId);
        final ConfigurationModel newModel = new ConfigurationModel(descriptorId, configId, configContext);
        final List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (final FieldValueEntity fieldValueEntity : fieldValueEntities) {
            final DefinedFieldEntity definedFieldEntity = definedFieldRepository
                                                              .findById(fieldValueEntity.getFieldId())
                                                              .orElseThrow(() -> new AlertDatabaseConstraintException("Field id cannot be null"));
            final String fieldKey = definedFieldEntity.getKey();
            final ConfigurationFieldModel fieldModel = definedFieldEntity.getSensitive() ? ConfigurationFieldModel.createSensitive(fieldKey) : ConfigurationFieldModel.create(fieldKey);
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

    private Long getConfigContextIdOrThrowException(final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (context == null) {
            throw new AlertDatabaseConstraintException("Context cannot be null");
        }
        return configContextRepository
                   .findFirstByContext(context.name())
                   .map(ConfigContextEntity::getId)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("That context does not exist"));
    }

    private String getContextById(final Long contextId) throws AlertDatabaseConstraintException {
        return configContextRepository
                   .findById(contextId)
                   .map(ConfigContextEntity::getContext)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No context with that id exists"));
    }

    private Long getFieldIdOrThrowException(final String fieldKey) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(fieldKey)) {
            throw new AlertDatabaseConstraintException("Field key cannot be empty");
        }
        return definedFieldRepository
                   .findFirstByKey(fieldKey)
                   .map(DefinedFieldEntity::getId)
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
        private final ConfigContextEnum context;
        private final Map<String, ConfigurationFieldModel> configuredFields;

        private ConfigurationModel(final Long registeredDescriptorId, final Long descriptorConfigId, final String context) {
            this(registeredDescriptorId, descriptorConfigId, ConfigContextEnum.valueOf(context));
        }

        private ConfigurationModel(final Long registeredDescriptorId, final Long descriptorConfigId, final ConfigContextEnum context) {
            descriptorId = registeredDescriptorId;
            configurationId = descriptorConfigId;
            this.context = context;
            configuredFields = new HashMap<>();
        }

        public Long getDescriptorId() {
            return descriptorId;
        }

        public Long getConfigurationId() {
            return configurationId;
        }

        public ConfigContextEnum getDescriptorContext() {
            return context;
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