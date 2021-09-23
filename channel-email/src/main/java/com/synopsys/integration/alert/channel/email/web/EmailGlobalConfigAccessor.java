package com.synopsys.integration.alert.channel.email.web;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigAccessor implements ConfigurationAccessor<EmailGlobalConfigModel> {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final DescriptorConfigRepository descriptorConfigsRepository;
    private final ConfigContextRepository configContextRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public EmailGlobalConfigAccessor(
        RegisteredDescriptorRepository registeredDescriptorRepository,
        DefinedFieldRepository definedFieldRepository,
        DescriptorConfigRepository descriptorConfigsRepository,
        ConfigContextRepository configContextRepository,
        FieldValueRepository fieldValueRepository,
        EncryptionUtility encryptionUtility
    ) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configContextRepository = configContextRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public Optional<DatabaseModelWrapper<EmailGlobalConfigModel>> getConfiguration(Long id) {
        return descriptorConfigsRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    public List<DatabaseModelWrapper<EmailGlobalConfigModel>> getAllConfigurations() {
        Long contextId = getConfigContextIdOrThrowException(ConfigContextEnum.GLOBAL);
        Long descriptorId = getDescriptorIdOrThrowException(ChannelKeys.EMAIL.getUniversalKey());

        return descriptorConfigsRepository.findByDescriptorIdAndContextId(descriptorId, contextId)
                   .stream()
                   .map(this::createConfigModel)
                   .collect(Collectors.toList());
    }

    @Override
    public DatabaseModelWrapper<EmailGlobalConfigModel> createConfiguration(EmailGlobalConfigModel configuration) {
        Long contextId = getConfigContextIdOrThrowException(ConfigContextEnum.GLOBAL);
        Long descriptorId = getDescriptorIdOrThrowException(ChannelKeys.EMAIL.getUniversalKey());
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfigToSave = new DescriptorConfigEntity(descriptorId, contextId, currentTime, currentTime);
        DescriptorConfigEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        if (configuration != null) {
            List<FieldValueEntity> fieldValuesToSave = toFieldValueEntitites(descriptorId, configuration);
            fieldValueRepository.saveAll(fieldValuesToSave);
            fieldValueRepository.flush();
        }
        return createConfigModel(savedDescriptorConfig);
    }

    @Override
    public DatabaseModelWrapper<EmailGlobalConfigModel> updateConfiguration(Long configurationId, EmailGlobalConfigModel configuration) throws AlertConfigurationException {
        DescriptorConfigEntity descriptorConfigEntity = descriptorConfigsRepository.findById(configurationId)
            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%d' did not exist", configurationId)));
        List<FieldValueEntity> oldValues = fieldValueRepository.findByConfigId(configurationId);
        fieldValueRepository.deleteAll(oldValues);
        fieldValueRepository.flush();

        if (configuration != null) {
            List<FieldValueEntity> fieldValuesToSave = toFieldValueEntitites(configurationId, configuration);
            fieldValueRepository.saveAll(fieldValuesToSave);
            fieldValueRepository.flush();
        }
        descriptorConfigEntity.setLastUpdated(DateUtils.createCurrentDateTimestamp());
        DescriptorConfigEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigEntity);

        return createConfigModel(savedDescriptorConfig);
    }

    @Override
    public void deleteConfiguration(Long configurationId) {
        if (null != configurationId) {
            descriptorConfigsRepository.deleteById(configurationId);
        }
    }

    private DatabaseModelWrapper<EmailGlobalConfigModel> createConfigModel(DescriptorConfigEntity descriptorConfigEntity) {
        return createConfigModel(
            descriptorConfigEntity.getDescriptorId(),
            descriptorConfigEntity.getId(),
            descriptorConfigEntity.getCreatedAt(),
            descriptorConfigEntity.getLastUpdated()
        );
    }


    private DatabaseModelWrapper<EmailGlobalConfigModel> createConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        EmailGlobalConfigModel newModel = new EmailGlobalConfigModel();
        Map<String, String> additionalJavamailProperties = new HashMap<>();
        List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
        for (FieldValueEntity fieldValueEntity : fieldValueEntities) {
            DefinedFieldEntity definedFieldEntity = definedFieldRepository.findById(fieldValueEntity.getFieldId())
                .orElseThrow(() -> new AlertRuntimeException("Field Id missing from the database"));
            String key = definedFieldEntity.getKey();
            String decryptedValue = decrypt(fieldValueEntity.getValue(), BooleanUtils.isTrue(definedFieldEntity.getSensitive()));

            if (EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey().equals(key)) {
                newModel.setFrom(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey().equals(key)) {
                newModel.setHost(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey().equals(key)) {
                newModel.setPort(Integer.valueOf(decryptedValue));
            } else if (EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey().equals(key)) {
                newModel.setAuth(Boolean.valueOf(decryptedValue));
            } else if (EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey().equals(key)) {
                newModel.setUsername(decryptedValue);
            } else if (EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(key)) {
                newModel.setPassword(decryptedValue);
            } else {
                additionalJavamailProperties.put(key, decryptedValue);
            }
        }
        newModel.setAdditionalJavaMailProperties(additionalJavamailProperties);
        newModel.setId(String.valueOf(configId));

        return new DatabaseModelWrapper<>(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, newModel);
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

    private List<FieldValueEntity> toFieldValueEntitites(Long configurationId, EmailGlobalConfigModel configuration) {
        List<FieldValueEntity> fieldValuesToSave = configuration.getAdditionalJavaMailProperties()
            .map(Map::entrySet)
            .stream()
            .flatMap(Collection::stream)
            .map(entry -> toValidFieldValueEntity(configurationId, entry.getKey(), entry.getValue(), false))
            .collect(Collectors.toCollection(ArrayList::new));

        configuration.getFrom()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), value, false))
            .ifPresent(fieldValuesToSave::add);
        configuration.getHost()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), value, false))
            .ifPresent(fieldValuesToSave::add);
        configuration.getPort()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), String.valueOf(value), false))
            .ifPresent(fieldValuesToSave::add);
        configuration.getAuth()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), String.valueOf(value), false))
            .ifPresent(fieldValuesToSave::add);
        configuration.getUsername()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), value, false))
            .ifPresent(fieldValuesToSave::add);
        configuration.getPassword()
            .map(value -> toValidFieldValueEntity(configurationId, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), value, true))
            .ifPresent(fieldValuesToSave::add);

        return fieldValuesToSave;
    }

    private FieldValueEntity toValidFieldValueEntity(Long configId, String key, String value, boolean isSensitve) {
        // TODO: How to store arbitrary data?
        DefinedFieldEntity associatedField = definedFieldRepository.findFirstByKey(key)
            .orElseThrow(() -> new AlertRuntimeException(String.format("FATAL: Field with key '%s' did not exist", key)));
        return new FieldValueEntity(configId, associatedField.getId(), encrypt(value, isSensitve));
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
