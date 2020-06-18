/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.datastructure.SetMap;

@Component
@Transactional
public class DefaultConfigurationAccessor implements ConfigurationAccessor {
    public static final String EXCEPTION_FORMAT_DESCRIPTOR_KEY_IS_NOT_VALID = "DescriptorKey is not valid. %s";
    private static final String NULL_JOB_ID = "The job id cannot be null";
    private static final String NULL_CONFIG_ID = "The config id cannot be null";
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigGroupRepository configGroupRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public DefaultConfigurationAccessor(RegisteredDescriptorRepository registeredDescriptorRepository, DescriptorTypeRepository descriptorTypeRepository, DefinedFieldRepository definedFieldRepository,
        DescriptorConfigRepository descriptorConfigsRepository, ConfigGroupRepository configGroupRepository, ConfigContextRepository configContextRepository, FieldValueRepository fieldValueRepository,
        EncryptionUtility encryptionUtility) {
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
        List<ConfigGroupEntity> jobEntities = configGroupRepository.findAll();
        SetMap<UUID, ConfigGroupEntity> jobMap = SetMap.createDefault();
        for (ConfigGroupEntity entity : jobEntities) {
            UUID entityJobId = entity.getJobId();
            jobMap.add(entityJobId, entity);
        }

        return jobMap.entrySet()
                   .stream()
                   .map(entry -> createJobModelFromExistingConfigs(entry.getKey(), entry.getValue()))
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }
        List<ConfigGroupEntity> jobConfigEntities = configGroupRepository.findByJobId(jobId);
        return jobConfigEntities
                   .stream()
                   .findAny()
                   .map(configGroupEntity -> createJobModelFromExistingConfigs(configGroupEntity.getJobId(), jobConfigEntities));
    }

    @Override
    public List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency) {
        return getAllJobs()
                   .stream()
                   .filter(job -> frequency == job.getFrequencyType())
                   .collect(Collectors.toList());
    }

    @Override
    public ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        return createJob(null, descriptorNames, configuredFields);
    }

    @Override

    public ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }

        deleteJob(jobId);
        return createJob(jobId, descriptorNames, configuredFields);
    }

    @Override
    public void deleteJob(UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }
        List<Long> configIdsForJob = configGroupRepository
                                         .findByJobId(jobId)
                                         .stream()
                                         .map(ConfigGroupEntity::getConfigId)
                                         .collect(Collectors.toList());
        for (Long configId : configIdsForJob) {
            deleteConfiguration(configId);
        }
    }

    @Override
    public Optional<ConfigurationModel> getProviderConfigurationByName(String providerConfigName) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(providerConfigName)) {
            throw new AlertDatabaseConstraintException("The provider configuration name cannot be null");
        }
        Long fieldId = definedFieldRepository.findFirstByKey(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                           .map(DefinedFieldEntity::getId)
                           .orElseThrow(() -> new AlertDatabaseConstraintException(String.format("The key '%s' is not registered in the database", ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)));
        List<Long> providerConfigIds = fieldValueRepository.findAllByFieldIdAndValue(fieldId, providerConfigName)
                                           .stream()
                                           .map(FieldValueEntity::getConfigId)
                                           .collect(Collectors.toList());
        if (!providerConfigIds.isEmpty()) {
            for (Long configId : providerConfigIds) {
                Optional<ConfigurationModel> globalModel = getConfigurationById(configId)
                                                               .filter(model -> model.getDescriptorContext() == ConfigContextEnum.GLOBAL);
                if (globalModel.isPresent()) {
                    return globalModel;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ConfigurationModel> getConfigurationById(Long id) throws AlertDatabaseConstraintException {
        if (id == null) {
            throw new AlertDatabaseConstraintException(NULL_CONFIG_ID);
        }
        Optional<DescriptorConfigEntity> optionalDescriptorConfigEntity = descriptorConfigsRepository.findById(id);
        if (optionalDescriptorConfigEntity.isPresent()) {
            DescriptorConfigEntity descriptorConfigEntity = optionalDescriptorConfigEntity.get();
            return Optional
                       .of(createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(), descriptorConfigEntity.getCreatedAt(), descriptorConfigEntity.getLastUpdated(), descriptorConfigEntity.getContextId()));
        }
        return Optional.empty();
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException {
        if (null == descriptorKey || StringUtils.isBlank(descriptorKey.getUniversalKey())) {
            throw new AlertDatabaseConstraintException(String.format(EXCEPTION_FORMAT_DESCRIPTOR_KEY_IS_NOT_VALID, descriptorKey));
        }
        Optional<RegisteredDescriptorEntity> registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey());
        if (registeredDescriptorEntity.isPresent()) {
            return createConfigModels(Collections.singleton(registeredDescriptorEntity.get()));
        }
        return createConfigModels(Collections.emptyList());
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorType(DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        if (null == descriptorType) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be null");
        }

        Long typeId = descriptorTypeRepository
                          .findFirstByType(descriptorType.name())
                          .map(DatabaseEntity::getId)
                          .orElseThrow(() -> new AlertDatabaseConstraintException("Descriptor type has not been registered"));
        List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeId(typeId);
        return createConfigModels(registeredDescriptorEntities);
    }

    @Override
    public List<ConfigurationModel> getChannelConfigurationsByFrequency(FrequencyType frequencyType) throws AlertDatabaseConstraintException {
        if (null == frequencyType) {
            throw new AlertDatabaseConstraintException("Frequency type cannot be null");
        }
        Long typeId = descriptorTypeRepository
                          .findFirstByType(DescriptorType.CHANNEL.name())
                          .map(DatabaseEntity::getId)
                          .orElseThrow(() -> new AlertDatabaseConstraintException("Descriptor type has not been registered"));
        List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeIdAndFrequency(typeId, frequencyType.name());
        return createConfigModels(registeredDescriptorEntities);
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorKeyAndContext(DescriptorKey descriptorKey, ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (null == descriptorKey || StringUtils.isBlank(descriptorKey.getUniversalKey())) {
            throw new AlertDatabaseConstraintException(String.format(EXCEPTION_FORMAT_DESCRIPTOR_KEY_IS_NOT_VALID, descriptorKey));
        }
        return getConfigurationsByDescriptorNameAndContext(descriptorKey.getUniversalKey(), context);
    }

    @Override
    public ConfigurationModel createConfiguration(DescriptorKey descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (null == descriptorKey || StringUtils.isBlank(descriptorKey.getUniversalKey())) {
            throw new AlertDatabaseConstraintException(String.format(EXCEPTION_FORMAT_DESCRIPTOR_KEY_IS_NOT_VALID, descriptorKey));
        }
        return createConfiguration(descriptorKey.getUniversalKey(), context, configuredFields);
    }

    private ConfigurationModel createConfiguration(String descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorKey)) {
            throw new AlertDatabaseConstraintException("DescriptorKey cannot be null");
        }
        Long descriptorId = getDescriptorIdOrThrowException(descriptorKey);
        Long configContextId = getConfigContextIdOrThrowException(context);
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfigToSave = new DescriptorConfigEntity(descriptorId, configContextId, currentTime, currentTime);
        DescriptorConfigEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        ConfigurationModelMutable createdConfig = createEmptyConfigModel(descriptorId, savedDescriptorConfig.getId(), savedDescriptorConfig.getCreatedAt(), savedDescriptorConfig.getLastUpdated(), context);
        if (configuredFields != null && !configuredFields.isEmpty()) {
            List<FieldValueEntity> fieldValuesToSave = new ArrayList<>(configuredFields.size());
            for (ConfigurationFieldModel configuredField : configuredFields) {
                String fieldKey = configuredField.getFieldKey();
                if (configuredField.isSet()) {
                    DefinedFieldEntity associatedField = definedFieldRepository
                                                             .findFirstByKey(fieldKey)
                                                             .orElseThrow(() -> new AlertDatabaseConstraintException("A field with the provided key did not exist: " + fieldKey));
                    for (String value : configuredField.getFieldValues()) {
                        FieldValueEntity newFieldValueEntity = new FieldValueEntity(createdConfig.getConfigurationId(), associatedField.getId(), encrypt(value, configuredField.isSensitive()));
                        fieldValuesToSave.add(newFieldValueEntity);
                    }
                }
                createdConfig.put(configuredField);
            }
            fieldValueRepository.saveAll(fieldValuesToSave);
        }
        return createdConfig;
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be null");
        }
        if (null == context) {
            throw new AlertDatabaseConstraintException("Context cannot be null");
        }

        Long contextId = getConfigContextIdOrThrowException(context);
        Long descriptorId = getDescriptorIdOrThrowException(descriptorName);

        List<DescriptorConfigEntity> descriptorConfigEntities = descriptorConfigsRepository.findByDescriptorIdAndContextId(descriptorId, contextId);

        List<ConfigurationModel> configurationModels = new ArrayList<>();

        for (DescriptorConfigEntity descriptorConfigEntity : descriptorConfigEntities) {
            configurationModels.add(createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(), descriptorConfigEntity.getCreatedAt(),
                descriptorConfigEntity.getLastUpdated(), contextId));
        }
        return configurationModels;
    }

    /**
     * @return the config after update
     */
    @Override
    public ConfigurationModel updateConfiguration(Long descriptorConfigId, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException(NULL_CONFIG_ID);
        }
        DescriptorConfigEntity descriptorConfigEntity = descriptorConfigsRepository
                                                            .findById(descriptorConfigId)
                                                            .orElseThrow(() -> new AlertDatabaseConstraintException("A config with that id did not exist"));

        List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(descriptorConfigId);
        fieldValueRepository.deleteAll(oldValues);

        ConfigurationModelMutable updatedConfig = createEmptyConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(),
            descriptorConfigEntity.getCreatedAt(), descriptorConfigEntity.getLastUpdated(), descriptorConfigEntity.getContextId());
        if (configuredFields != null && !configuredFields.isEmpty()) {
            List<FieldValueEntity> fieldValuesToSave = new ArrayList<>(configuredFields.size());
            for (ConfigurationFieldModel configFieldModel : configuredFields) {
                Long fieldId = getFieldIdOrThrowException(configFieldModel.getFieldKey());
                for (String value : configFieldModel.getFieldValues()) {
                    FieldValueEntity newFieldValue = new FieldValueEntity(descriptorConfigId, fieldId, encrypt(value, configFieldModel.isSensitive()));
                    fieldValuesToSave.add(newFieldValue);
                }
                updatedConfig.put(configFieldModel);
            }
            fieldValueRepository.saveAll(fieldValuesToSave);
        }
        descriptorConfigEntity.setLastUpdated(DateUtils.createCurrentDateTimestamp());
        descriptorConfigsRepository.save(descriptorConfigEntity);

        return updatedConfig;
    }

    @Override
    public void deleteConfiguration(ConfigurationModel configModel) throws AlertDatabaseConstraintException {
        if (configModel == null) {
            throw new AlertDatabaseConstraintException("Cannot delete a null object from the database");
        }
        deleteConfiguration(configModel.getConfigurationId());
    }

    @Override
    public void deleteConfiguration(Long descriptorConfigId) throws AlertDatabaseConstraintException {
        if (descriptorConfigId == null) {
            throw new AlertDatabaseConstraintException(NULL_CONFIG_ID);
        }
        descriptorConfigsRepository.deleteById(descriptorConfigId);
    }

    private ConfigurationJobModel createJob(UUID oldJobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorNames == null || descriptorNames.isEmpty()) {
            throw new AlertDatabaseConstraintException("Descriptor names cannot be empty");
        }
        Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (String descriptorName : descriptorNames) {
            configurationModels.add(createConfigForRelevantFields(descriptorName, configuredFields));
        }

        UUID newJobId = oldJobId;
        if (newJobId == null) {
            newJobId = UUID.randomUUID();
        }

        List<ConfigGroupEntity> configGroupsToSave = new ArrayList<>(configurationModels.size());
        for (ConfigurationModel createdModel : configurationModels) {
            ConfigGroupEntity configGroupEntityToSave = new ConfigGroupEntity(createdModel.getConfigurationId(), newJobId);
            configGroupsToSave.add(configGroupEntityToSave);
        }
        configGroupRepository.saveAll(configGroupsToSave);
        return new ConfigurationJobModel(newJobId, configurationModels);
    }

    private ConfigurationJobModel createJobModelFromExistingConfigs(UUID jobId, Collection<ConfigGroupEntity> entities) {
        Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (ConfigGroupEntity sortedEntity : entities) {
            try {
                getConfigurationById(sortedEntity.getConfigId()).ifPresent(configurationModels::add);
            } catch (AlertDatabaseConstraintException e) {
                // This case should be impossible based on database constraints
                throw new AlertRuntimeException(e);
            }
        }
        return new ConfigurationJobModel(jobId, configurationModels);
    }

    private ConfigurationModel createConfigForRelevantFields(String descriptorName, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        Long contextId = getConfigContextIdOrThrowException(ConfigContextEnum.DISTRIBUTION);
        Set<String> descriptorFields = definedFieldRepository.findByDescriptorIdAndContext(descriptorId, contextId)
                                           .stream()
                                           .map(DefinedFieldEntity::getKey)
                                           .collect(Collectors.toSet());

        Set<ConfigurationFieldModel> relevantFields = configuredFields
                                                          .stream()
                                                          .filter(field -> descriptorFields.contains(field.getFieldKey()))
                                                          .collect(Collectors.toSet());
        return createConfiguration(descriptorName, ConfigContextEnum.DISTRIBUTION, relevantFields);
    }

    private List<ConfigurationModel> createConfigModels(Collection<RegisteredDescriptorEntity> descriptors) throws AlertDatabaseConstraintException {
        List<ConfigurationModel> configs = new ArrayList<>();
        for (RegisteredDescriptorEntity descriptorEntity : descriptors) {
            List<DescriptorConfigEntity> descriptorConfigEntities = descriptorConfigsRepository.findByDescriptorId(descriptorEntity.getId());
            for (DescriptorConfigEntity descriptorConfigEntity : descriptorConfigEntities) {
                ConfigurationModel newModel = createConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(),
                    descriptorConfigEntity.getCreatedAt(), descriptorConfigEntity.getLastUpdated(), descriptorConfigEntity.getContextId());
                configs.add(newModel);
            }
        }
        return configs;
    }

    private ConfigurationModelMutable createConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, Long contextId) throws AlertDatabaseConstraintException {
        String configContext = getContextById(contextId);

        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        ConfigurationModelMutable newModel = new ConfigurationModelMutable(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, configContext);
        List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (FieldValueEntity fieldValueEntity : fieldValueEntities) {
            DefinedFieldEntity definedFieldEntity = definedFieldRepository
                                                        .findById(fieldValueEntity.getFieldId())
                                                        .orElseThrow(() -> new AlertDatabaseConstraintException("Field id cannot be null"));
            String fieldKey = definedFieldEntity.getKey();
            ConfigurationFieldModel fieldModel = BooleanUtils.isTrue(definedFieldEntity.getSensitive()) ? ConfigurationFieldModel.createSensitive(fieldKey) : ConfigurationFieldModel.create(fieldKey);
            String decryptedValue = decrypt(fieldValueEntity.getValue(), fieldModel.isSensitive());
            fieldModel.setFieldValue(decryptedValue);
            newModel.put(fieldModel);
        }
        return newModel;
    }

    private ConfigurationModelMutable createEmptyConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, Long contextId) throws AlertDatabaseConstraintException {
        String configContext = getContextById(contextId);

        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        return new ConfigurationModelMutable(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, configContext);
    }

    private ConfigurationModelMutable createEmptyConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, ConfigContextEnum context) {
        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        return new ConfigurationModelMutable(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, context);
    }

    private Long getDescriptorIdOrThrowException(String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorRepository
                   .findFirstByName(descriptorName)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor with the provided name was registered"));
    }

    private Long getConfigContextIdOrThrowException(ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (context == null) {
            throw new AlertDatabaseConstraintException("Context cannot be null");
        }
        return configContextRepository
                   .findFirstByContext(context.name())
                   .map(ConfigContextEntity::getId)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("That context does not exist"));
    }

    private String getContextById(Long contextId) throws AlertDatabaseConstraintException {
        return configContextRepository
                   .findById(contextId)
                   .map(ConfigContextEntity::getContext)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No context with that id exists"));
    }

    private Long getFieldIdOrThrowException(String fieldKey) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(fieldKey)) {
            throw new AlertDatabaseConstraintException("Field key cannot be empty");
        }
        return definedFieldRepository
                   .findFirstByKey(fieldKey)
                   .map(DefinedFieldEntity::getId)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("A field with that key did not exist"));
    }

    private String encrypt(String value, boolean shouldEncrypt) {
        if (shouldEncrypt && value != null) {
            return encryptionUtility.encrypt(value);
        }
        return value;
    }

    private String decrypt(String value, boolean shouldDecrypt) {
        if (shouldDecrypt && value != null) {
            return encryptionUtility.decrypt(value);
        }
        return value;
    }

}
