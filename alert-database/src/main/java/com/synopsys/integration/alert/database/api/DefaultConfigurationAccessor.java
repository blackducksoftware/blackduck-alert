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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
@Transactional
public class DefaultConfigurationAccessor implements ConfigurationAccessor {
    private static final String NULL_CONFIG_ID = "The config id cannot be null";

    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public DefaultConfigurationAccessor(
        RegisteredDescriptorRepository registeredDescriptorRepository,
        DescriptorTypeRepository descriptorTypeRepository,
        DefinedFieldRepository definedFieldRepository,
        DescriptorConfigRepository descriptorConfigsRepository,
        ConfigContextRepository configContextRepository,
        FieldValueRepository fieldValueRepository,
        EncryptionUtility encryptionUtility
    ) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorTypeRepository = descriptorTypeRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configContextRepository = configContextRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public Optional<ConfigurationModel> getProviderConfigurationByName(String providerConfigName) {
        if (StringUtils.isBlank(providerConfigName)) {
            return Optional.empty();
        }

        List<Long> providerConfigIds = definedFieldRepository.findFirstByKey(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                                           .map(DefinedFieldEntity::getId)
                                           .stream()
                                           .map(fieldId -> fieldValueRepository.findAllByFieldIdAndValue(fieldId, providerConfigName))
                                           .flatMap(List::stream)
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
    public Optional<ConfigurationModel> getConfigurationById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        Optional<DescriptorConfigEntity> optionalDescriptorConfigEntity = descriptorConfigsRepository.findById(id);
        if (optionalDescriptorConfigEntity.isPresent()) {
            DescriptorConfigEntity descriptorConfigEntity = optionalDescriptorConfigEntity.get();
            ConfigurationModelMutable configModel = createConfigModel(
                descriptorConfigEntity.getDescriptorId(),
                descriptorConfigEntity.getId(),
                descriptorConfigEntity.getCreatedAt(),
                descriptorConfigEntity.getLastUpdated(),
                descriptorConfigEntity.getContextId()
            );
            return Optional.of(configModel);
        }
        return Optional.empty();
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorKey(DescriptorKey descriptorKey) {
        return registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())
                   .map(descriptorEntity -> createConfigModels(Set.of(descriptorEntity)))
                   .orElseGet(() -> createConfigModels(Set.of()));
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorType(DescriptorType descriptorType) {
        String descriptorTypeName = descriptorType.name();
        Long typeId = descriptorTypeRepository.findFirstByType(descriptorTypeName)
                          .map(DatabaseEntity::getId)
                          .orElseThrow(() -> new AlertRuntimeException(String.format("FATAL: Descriptor type '%s' does not exist", descriptorTypeName)));
        List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeId(typeId);
        return createConfigModels(registeredDescriptorEntities);
    }

    @Override
    public List<ConfigurationModel> getChannelConfigurationsByFrequency(FrequencyType frequencyType) {
        String channelDescriptorTypeName = DescriptorType.CHANNEL.name();
        Long typeId = descriptorTypeRepository.findFirstByType(channelDescriptorTypeName)
                          .map(DatabaseEntity::getId)
                          .orElseThrow(() -> new AlertRuntimeException(String.format("FATAL: Descriptor type '%s' does not exist", channelDescriptorTypeName)));
        List<RegisteredDescriptorEntity> registeredDescriptorEntities = registeredDescriptorRepository.findByTypeIdAndFrequency(typeId, frequencyType.name());
        return createConfigModels(registeredDescriptorEntities);
    }

    @Override
    public List<ConfigurationModel> getConfigurationsByDescriptorKeyAndContext(DescriptorKey descriptorKey, ConfigContextEnum context) {
        return getConfigurationsByDescriptorNameAndContext(descriptorKey.getUniversalKey(), context);
    }

    @Override
    public ConfigurationModel createConfiguration(DescriptorKey descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) {
        return createConfiguration(descriptorKey.getUniversalKey(), context, configuredFields);
    }

    private ConfigurationModel createConfiguration(String descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields) {
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
                    DefinedFieldEntity associatedField = definedFieldRepository.findFirstByKey(fieldKey)
                                                             .orElseThrow(() -> new AlertRuntimeException(String.format("FATAL: Field with key '%s' did not exist", fieldKey)));
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
    public List<ConfigurationModel> getConfigurationsByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context) {
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
    public ConfigurationModel updateConfiguration(Long descriptorConfigId, Collection<ConfigurationFieldModel> configuredFields) throws AlertConfigurationException {
        DescriptorConfigEntity descriptorConfigEntity = descriptorConfigsRepository.findById(descriptorConfigId)
                                                            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%d' did not exist", descriptorConfigId)));
        List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(descriptorConfigId);
        fieldValueRepository.deleteAll(oldValues);
        fieldValueRepository.flush();

        ConfigurationModelMutable updatedConfig = createEmptyConfigModel(descriptorConfigEntity.getDescriptorId(), descriptorConfigEntity.getId(),
            descriptorConfigEntity.getCreatedAt(), descriptorConfigEntity.getLastUpdated(), descriptorConfigEntity.getContextId());
        if (configuredFields != null && !configuredFields.isEmpty()) {
            List<FieldValueEntity> fieldValuesToSave = new ArrayList<>(configuredFields.size());
            for (ConfigurationFieldModel configFieldModel : configuredFields) {
                String fieldKey = configFieldModel.getFieldKey();
                Long fieldId = getFieldIdOrThrowException(fieldKey);
                boolean isSensitive = isFieldSensitive(fieldKey);
                for (String value : configFieldModel.getFieldValues()) {
                    FieldValueEntity newFieldValue = new FieldValueEntity(descriptorConfigId, fieldId, encrypt(value, isSensitive));
                    fieldValuesToSave.add(newFieldValue);
                }
                updatedConfig.put(configFieldModel);
            }
            fieldValueRepository.saveAll(fieldValuesToSave);
            fieldValueRepository.flush();
        }
        descriptorConfigEntity.setLastUpdated(DateUtils.createCurrentDateTimestamp());
        descriptorConfigsRepository.save(descriptorConfigEntity);

        return updatedConfig;
    }

    @Override
    public void deleteConfiguration(ConfigurationModel configModel) {
        deleteConfiguration(configModel.getConfigurationId());
    }

    @Override
    public void deleteConfiguration(Long descriptorConfigId) {
        if (null != descriptorConfigId) {
            descriptorConfigsRepository.deleteById(descriptorConfigId);
        }
    }

    public ConfigurationModel createConfigForRelevantFields(String descriptorName, Collection<ConfigurationFieldModel> configuredFields) {
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

    private List<ConfigurationModel> createConfigModels(Collection<RegisteredDescriptorEntity> descriptors) {
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

    private ConfigurationModelMutable createConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, Long contextId) {
        String configContext = getContextById(contextId);

        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        ConfigurationModelMutable newModel = new ConfigurationModelMutable(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, configContext);
        List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (FieldValueEntity fieldValueEntity : fieldValueEntities) {
            DefinedFieldEntity definedFieldEntity = definedFieldRepository.findById(fieldValueEntity.getFieldId())
                                                        .orElseThrow(() -> new AlertRuntimeException("Field Id missing from the database"));
            String fieldKey = definedFieldEntity.getKey();
            ConfigurationFieldModel fieldModel = BooleanUtils.isTrue(definedFieldEntity.getSensitive()) ? ConfigurationFieldModel.createSensitive(fieldKey) : ConfigurationFieldModel.create(fieldKey);
            String decryptedValue = decrypt(fieldValueEntity.getValue(), fieldModel.isSensitive());
            fieldModel.setFieldValue(decryptedValue);
            newModel.put(fieldModel);
        }
        return newModel;
    }

    private ConfigurationModelMutable createEmptyConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, Long contextId) {
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

    private Long getDescriptorIdOrThrowException(String descriptorName) {
        return registeredDescriptorRepository.findFirstByName(descriptorName)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException(String.format("No descriptor with name '%s' exists", descriptorName)));
    }

    private Long getConfigContextIdOrThrowException(ConfigContextEnum context) {
        String contextName = context.name();
        return configContextRepository.findFirstByContext(contextName)
                   .map(ConfigContextEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException(String.format("No context with name '%s' exists", contextName)));
    }

    private String getContextById(Long contextId) {
        return configContextRepository.findById(contextId)
                   .map(ConfigContextEntity::getContext)
                   .orElseThrow(() -> new AlertRuntimeException(String.format("No context with id '%d' exists", contextId)));
    }

    private Long getFieldIdOrThrowException(String fieldKey) {
        return definedFieldRepository.findFirstByKey(fieldKey)
                   .map(DefinedFieldEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException(String.format("A field with key '%s' did not exist", fieldKey)));
    }

    private boolean isFieldSensitive(String fieldKey) {
        if (StringUtils.isBlank(fieldKey)) {
            return false;
        }
        return definedFieldRepository.findFirstByKey(fieldKey)
                   .map(DefinedFieldEntity::getSensitive)
                   .orElse(false);
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
