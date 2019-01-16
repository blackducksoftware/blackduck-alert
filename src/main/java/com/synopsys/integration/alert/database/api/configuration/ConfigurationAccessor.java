/**
 * blackduck-alert
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
package com.synopsys.integration.alert.database.api.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.ConfigGroupRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;

@Component
@Transactional
public class ConfigurationAccessor implements BaseConfigurationAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigGroupRepository configGroupRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public ConfigurationAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DescriptorTypeRepository descriptorTypeRepository, final DefinedFieldRepository definedFieldRepository,
        final DescriptorConfigRepository descriptorConfigsRepository, final ConfigGroupRepository configGroupRepository, final ConfigContextRepository configContextRepository, final FieldValueRepository fieldValueRepository,
        final EncryptionUtility encryptionUtility) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorTypeRepository = descriptorTypeRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configGroupRepository = configGroupRepository;
        this.configContextRepository = configContextRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public List<ConfigurationJobModel> getAllJobs() {
        final List<ConfigGroupEntity> jobEntities = configGroupRepository.findAll();
        final Map<UUID, Set<ConfigGroupEntity>> jobMap = new HashMap<>();
        for (final ConfigGroupEntity entity : jobEntities) {
            final UUID entityJobId = entity.getJobId();
            if (!jobMap.containsKey(entityJobId)) {
                jobMap.put(entityJobId, new HashSet<>());
            }
            jobMap.get(entityJobId).add(entity);
        }

        return jobMap.entrySet()
                   .stream()
                   .map(entry -> createJobModelFromExistingConfigs(entry.getKey(), entry.getValue()))
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(final UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException("The job id cannot be null");
        }
        final List<ConfigGroupEntity> jobConfigEntities = configGroupRepository.findByJobId(jobId);
        return jobConfigEntities
                   .stream()
                   .findAny()
                   .map(configGroupEntity -> createJobModelFromExistingConfigs(configGroupEntity.getJobId(), jobConfigEntities));
    }

    @Override
    public ConfigurationJobModel createJob(final Collection<String> descriptorNames, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        return createJob(null, descriptorNames, configuredFields);
    }

    @Override
    public ConfigurationJobModel updateJob(final UUID jobId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException("The job id cannot be null");
        }
        final Set<String> descriptorNames = registeredDescriptorRepository.findByJobId(jobId)
                                                .stream()
                                                .map(RegisteredDescriptorEntity::getName)
                                                .collect(Collectors.toSet());
        configGroupRepository.deleteByJobId(jobId);
        return createJob(jobId, descriptorNames, configuredFields);
    }

    @Override
    public void deleteJob(final UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException("The job id cannot be null");
        }
        configGroupRepository.deleteByJobId(jobId);
    }

    @Override
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

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (registeredDescriptorEntity.isPresent()) {
            return createConfigModels(Collections.singleton(registeredDescriptorEntity.get()));
        }
        return createConfigModels(Collections.emptyList());
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorType(final DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        if (null == descriptorType) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be null");
        }

        final Long typeId = descriptorTypeRepository
                                .findFirstByType(descriptorType.name())
                                .map(DatabaseEntity::getId)
                                .orElseThrow(() -> new AlertDatabaseConstraintException("Descriptor type has not been registered"));
        final List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeId(typeId);
        return createConfigModels(registeredDescriptorEntities);
    }

    @Override
    public List<ConfigurationModel> getChannelConfigurationsByFrequency(final FrequencyType frequencyType) throws AlertDatabaseConstraintException {
        if (null == frequencyType) {
            throw new AlertDatabaseConstraintException("Frequency type cannot be null");
        }
        final Long typeId = descriptorTypeRepository
                                .findFirstByType(DescriptorType.CHANNEL.name())
                                .map(DatabaseEntity::getId)
                                .orElseThrow(() -> new AlertDatabaseConstraintException("Descriptor type has not been registered"));
        final List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeIdAndFrequency(typeId, frequencyType.name());
        return createConfigModels(registeredDescriptorEntities);
    }

    // TODO change query to get Id originally
    @Override
    public List<ConfigurationModel> getConfigurationByDescriptorNameAndContext(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be null");
        }
        if (null == context) {
            throw new AlertDatabaseConstraintException("Context cannot be null");
        }

        final Long contextId = getConfigContextIdOrThrowException(context);
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final Set<Long> configurationIds = descriptorConfigsRepository
                                               .findByDescriptorIdAndContextId(descriptorId, contextId)
                                               .stream()
                                               .map(DescriptorConfigEntity::getId)
                                               .collect(Collectors.toSet());
        final List<ConfigurationModel> configurationModels = new ArrayList<>();
        for (final Long configId : configurationIds) {
            configurationModels.add(createConfigModel(descriptorId, configId, contextId));
        }
        return configurationModels;
    }

    /**
     * @return the config that was created
     */
    @Override
    public ConfigurationModel createEmptyConfiguration(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        return createConfiguration(descriptorName, context, null);
    }

    /**
     * @return the config that was created
     */
    @Override
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
                // TODO find out if this had a different result: createdConfig.configuredFields.put(fieldKey, configuredField);
                createdConfig.put(configuredField);
            }
        }
        return createdConfig;
    }

    /**
     * @return the config after update
     */
    @Override
    public ConfigurationModel updateConfiguration(final Long descriptorConfigId, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        final DescriptorConfigEntity descriptorConfigEntity = descriptorConfigsRepository
                                                                  .findById(descriptorConfigId)
                                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("A config with that id did not exist"));

        final List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(descriptorConfigId);
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

    @Override
    public void deleteConfiguration(final ConfigurationModel configModel) throws AlertDatabaseConstraintException {
        if (configModel == null) {
            throw new AlertDatabaseConstraintException("Cannot delete a null object from the database");
        }
        deleteConfiguration(configModel.getConfigurationId());
    }

    @Override
    public void deleteConfiguration(final Long descriptorConfigId) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException("The config id cannot be null");
        }
        descriptorConfigsRepository.deleteById(descriptorConfigId);
    }

    private ConfigurationJobModel createJob(final UUID oldJobId, final Collection<String> descriptorNames, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorNames == null || descriptorNames.isEmpty()) {
            throw new AlertDatabaseConstraintException("Descriptor names cannot be empty");
        }
        final Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (final String descriptorName : descriptorNames) {
            configurationModels.add(createConfigForRelevantFields(descriptorName, configuredFields));
        }

        UUID newJobId = oldJobId;
        if (newJobId == null) {
            newJobId = UUID.randomUUID();
        }
        for (final ConfigurationModel createdModel : configurationModels) {
            final ConfigGroupEntity configGroupEntityToSave = new ConfigGroupEntity(createdModel.getConfigurationId(), newJobId);
            configGroupRepository.save(configGroupEntityToSave);
        }
        return new ConfigurationJobModel(newJobId, configurationModels);
    }

    private ConfigurationJobModel createJobModelFromExistingConfigs(final UUID jobId, final Collection<ConfigGroupEntity> entities) {
        final Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (final ConfigGroupEntity sortedEntity : entities) {
            try {
                getConfigurationById(sortedEntity.getConfigId()).ifPresent(configurationModels::add);
            } catch (final AlertDatabaseConstraintException e) {
                // This case should be impossible based on database constraints
                throw new AlertRuntimeException(e);
            }
        }
        return new ConfigurationJobModel(jobId, configurationModels);
    }

    private ConfigurationModel createConfigForRelevantFields(final String descriptorName, final Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final Long contextId = getConfigContextIdOrThrowException(ConfigContextEnum.DISTRIBUTION);
        final Set<String> descriptorFields = definedFieldRepository.findByDescriptorIdAndContext(descriptorId, contextId)
                                                 .stream()
                                                 .map(DefinedFieldEntity::getKey)
                                                 .collect(Collectors.toSet());

        final Set<ConfigurationFieldModel> relevantFields = configuredFields
                                                                .stream()
                                                                .filter(field -> descriptorFields.contains(field.getFieldKey()))
                                                                .collect(Collectors.toSet());
        return createConfiguration(descriptorName, ConfigContextEnum.DISTRIBUTION, relevantFields);
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
        // TODO should empty fields be included?
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
        if (StringUtils.isBlank(descriptorName)) {
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
        if (StringUtils.isBlank(fieldKey)) {
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

}
