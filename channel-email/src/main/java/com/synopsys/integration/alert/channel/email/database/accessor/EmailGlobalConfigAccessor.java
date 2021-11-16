/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.accessor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.DatabaseModelWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.email.EmailConfigurationEntity;
import com.synopsys.integration.alert.database.email.EmailConfigurationRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationPropertiesRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationsPropertyEntity;
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
    private final EmailConfigurationRepository emailConfigurationRepository;
    private final EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository;

    @Autowired
    public EmailGlobalConfigAccessor(
        RegisteredDescriptorRepository registeredDescriptorRepository,
        DefinedFieldRepository definedFieldRepository,
        DescriptorConfigRepository descriptorConfigsRepository,
        ConfigContextRepository configContextRepository,
        FieldValueRepository fieldValueRepository,
        EncryptionUtility encryptionUtility,
        EmailConfigurationRepository emailConfigurationRepository,
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository
    ) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.configContextRepository = configContextRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.encryptionUtility = encryptionUtility;
        this.emailConfigurationRepository = emailConfigurationRepository;
        this.emailConfigurationPropertiesRepository = emailConfigurationPropertiesRepository;
    }

    @Override
    public Optional<DatabaseModelWrapper<EmailGlobalConfigModel>> getConfiguration(UUID id) {
        return emailConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    public AlertPagedModel<DatabaseModelWrapper<EmailGlobalConfigModel>> getConfigurationPage(int page, int size) {
        Page<EmailConfigurationEntity> resultPage = emailConfigurationRepository.findAll(PageRequest.of(page, size));
        List<DatabaseModelWrapper<EmailGlobalConfigModel>> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    public DatabaseModelWrapper<EmailGlobalConfigModel> createConfiguration(EmailGlobalConfigModel configuration) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        EmailConfigurationEntity configurationToSave = toEntity(configuration, currentTime, currentTime);
        EmailConfigurationEntity savedEmailConfig = emailConfigurationRepository.save(configurationToSave);
        List<EmailConfigurationsPropertyEntity> emailProperties = toPropertyEntityList(configuration);
        emailConfigurationPropertiesRepository.saveAll(emailProperties);
        savedEmailConfig = emailConfigurationRepository.getOne(savedEmailConfig.getConfigurationId());

        return createConfigModel(savedEmailConfig);
    }

    @Override
    public DatabaseModelWrapper<EmailGlobalConfigModel> updateConfiguration(UUID configurationId, EmailGlobalConfigModel configuration) throws AlertConfigurationException {
        EmailConfigurationEntity configurationEntity = emailConfigurationRepository.findById(configurationId)
            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%d' did not exist", configurationId)));
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        EmailConfigurationEntity configurationToSave = toEntity(configuration, configurationEntity.getCreatedAt(), currentTime);
        EmailConfigurationEntity savedEmailConfig = emailConfigurationRepository.save(configurationToSave);
        List<EmailConfigurationsPropertyEntity> emailProperties = toPropertyEntityList(configuration);
        emailConfigurationPropertiesRepository.saveAll(emailProperties);
        savedEmailConfig = emailConfigurationRepository.getOne(savedEmailConfig.getConfigurationId());

        return createConfigModel(savedEmailConfig);
    }

    @Override
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            emailConfigurationRepository.deleteById(configurationId);
        }
    }

    private DatabaseModelWrapper<EmailGlobalConfigModel> createConfigModel(EmailConfigurationEntity emailConfiguration) {
        EmailGlobalConfigModel newModel = new EmailGlobalConfigModel();
        String createdAtFormatted = "";
        String lastUpdatedFormatted = "";
        if (null != emailConfiguration) {
            createdAtFormatted = DateUtils.formatDate(emailConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            if (null != emailConfiguration.getLastUpdated()) {
                lastUpdatedFormatted = DateUtils.formatDate(emailConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            }
            newModel.setHost(emailConfiguration.getSmtpHost());
            newModel.setPort(emailConfiguration.getSmtpPort().intValue());
            newModel.setFrom(emailConfiguration.getSmtpFrom());
            newModel.setAuth(emailConfiguration.getAuthRequired());
            newModel.setUsername(emailConfiguration.getAuthUsername());
            newModel.setPassword(encryptionUtility.decrypt(emailConfiguration.getAuthPassword()));
            newModel.setAdditionalJavaMailProperties(getAdditionalProperties(emailConfiguration.getEmailConfigurationProperties()));
        }

        Long descriptorId = getDescriptorIdOrThrowException();

        newModel.setId(String.valueOf(emailConfiguration.getConfigurationId()));
        newModel.setCreatedAt(createdAtFormatted);
        newModel.setLastUpdated(lastUpdatedFormatted);

        return new DatabaseModelWrapper<>(descriptorId, emailConfiguration.getConfigurationId(), createdAtFormatted, lastUpdatedFormatted, newModel);
    }

    private Map<String, String> getAdditionalProperties(List<EmailConfigurationsPropertyEntity> emailConfigurationProperties) {
        return emailConfigurationProperties.stream()
            .collect(Collectors.toMap(EmailConfigurationsPropertyEntity::getPropertyKey, EmailConfigurationsPropertyEntity::getPropertyValue));
    }

    private EmailConfigurationEntity toEntity(EmailGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        UUID configurationId = null;
        if (StringUtils.isNotBlank(configuration.getId())) {
            configurationId = UUID.fromString(configuration.getId());
        }
        String host = configuration.getHost().orElseThrow(null);
        String from = configuration.getFrom().orElse(null);
        Long port = configuration.getPort().map(Number::longValue).orElse(null);
        Boolean auth = configuration.getAuth().orElse(Boolean.FALSE);
        String username = configuration.getUsername().orElse(null);
        String password = configuration.getPassword().map(encryptionUtility::encrypt).orElse(null);

        return new EmailConfigurationEntity(configurationId, createdTime, lastUpdated,
            host, from, port, auth, username, password, toPropertyEntityList(configuration));
    }

    private List<EmailConfigurationsPropertyEntity> toPropertyEntityList(EmailGlobalConfigModel configuration) {
        if (StringUtils.isBlank(configuration.getId())) {
            return List.of();
        }
        UUID configurationId = UUID.fromString(configuration.getId());
        Map<String, String> emailProperties = configuration.getAdditionalJavaMailProperties().orElse(Map.of());
        return emailProperties.entrySet().stream()
            .map(entry -> new EmailConfigurationsPropertyEntity(configurationId, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    //    private DatabaseModelWrapper<EmailGlobalConfigModel> createConfigModel(Long descriptorId, Long configId, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
    //        String createdAtFormatted = DateUtils.formatDate(createdAt, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
    //        String lastUpdatedFormatted = DateUtils.formatDate(lastUpdated, DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
    //
    //        EmailGlobalConfigModel newModel = new EmailGlobalConfigModel();
    //        Map<String, String> additionalJavamailProperties = new HashMap<>();
    //
    //        List<FieldValueEntity> fieldValueEntities = fieldValueRepository.findByConfigId(configId);
    //        for (FieldValueEntity fieldValueEntity : fieldValueEntities) {
    //            DefinedFieldEntity definedFieldEntity = definedFieldRepository.findById(fieldValueEntity.getFieldId())
    //                .orElseThrow(() -> new AlertRuntimeException("Field Id missing from the database"));
    //            String key = definedFieldEntity.getKey();
    //            String decryptedValue = decrypt(fieldValueEntity.getValue(), BooleanUtils.isTrue(definedFieldEntity.getSensitive()));
    //
    //            if (EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey().equals(key)) {
    //                newModel.setFrom(decryptedValue);
    //            } else if (EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey().equals(key)) {
    //                newModel.setHost(decryptedValue);
    //            } else if (EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey().equals(key)) {
    //                newModel.setPort(Integer.valueOf(decryptedValue));
    //            } else if (EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey().equals(key)) {
    //                newModel.setAuth(Boolean.valueOf(decryptedValue));
    //            } else if (EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey().equals(key)) {
    //                newModel.setUsername(decryptedValue);
    //            } else if (EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(key)) {
    //                newModel.setPassword(decryptedValue);
    //            } else {
    //                additionalJavamailProperties.put(key, decryptedValue);
    //            }
    //        }
    //        newModel.setAdditionalJavaMailProperties(additionalJavamailProperties);
    //        newModel.setId(String.valueOf(configId));
    //        newModel.setCreatedAt(createdAtFormatted);
    //        newModel.setLastUpdated(lastUpdatedFormatted);
    //
    //        return new DatabaseModelWrapper<>(descriptorId, configId, createdAtFormatted, lastUpdatedFormatted, newModel);
    //    }

    private Long getDescriptorIdOrThrowException() {
        String descriptorName = ChannelKeys.EMAIL.getUniversalKey();
        return registeredDescriptorRepository.findFirstByName(descriptorName)
            .map(RegisteredDescriptorEntity::getId)
            .orElseThrow(() -> new AlertRuntimeException(String.format("No descriptor with name '%s' exists", descriptorName)));
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
        // FIXME: This will not support any JavaMail properties we do not currently populate in the definedFieldRepository-- see IALERT-2616 --rotte SEPT 2021
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
