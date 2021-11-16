/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.accessor;

import java.time.OffsetDateTime;
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
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
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
    public Optional<EmailGlobalConfigModel> getConfiguration(UUID id) {
        return emailConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    public AlertPagedModel<EmailGlobalConfigModel> getConfigurationPage(int page, int size) {
        Page<EmailConfigurationEntity> resultPage = emailConfigurationRepository.findAll(PageRequest.of(page, size));
        List<EmailGlobalConfigModel> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    public EmailGlobalConfigModel createConfiguration(EmailGlobalConfigModel configuration) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        EmailConfigurationEntity configurationToSave = toEntity(configuration, currentTime, currentTime);
        EmailConfigurationEntity savedEmailConfig = emailConfigurationRepository.save(configurationToSave);
        List<EmailConfigurationsPropertyEntity> emailProperties = toPropertyEntityList(configuration);
        emailConfigurationPropertiesRepository.saveAll(emailProperties);
        savedEmailConfig = emailConfigurationRepository.getOne(savedEmailConfig.getConfigurationId());

        return createConfigModel(savedEmailConfig);
    }

    @Override
    public EmailGlobalConfigModel updateConfiguration(UUID configurationId, EmailGlobalConfigModel configuration) throws AlertConfigurationException {
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

    private EmailGlobalConfigModel createConfigModel(EmailConfigurationEntity emailConfiguration) {
        EmailGlobalConfigModel newModel = new EmailGlobalConfigModel();
        String createdAtFormatted = "";
        String lastUpdatedFormatted = "";
        if (null != emailConfiguration) {
            createdAtFormatted = DateUtils.formatDate(emailConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            if (null != emailConfiguration.getLastUpdated()) {
                lastUpdatedFormatted = DateUtils.formatDate(emailConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            }
            newModel.setHost(emailConfiguration.getSmtpHost());
            newModel.setPort(emailConfiguration.getSmtpPort());
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

        return newModel;
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
        Integer port = configuration.getPort().orElse(null);
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

    private Long getDescriptorIdOrThrowException() {
        String descriptorName = ChannelKeys.EMAIL.getUniversalKey();
        return registeredDescriptorRepository.findFirstByName(descriptorName)
            .map(RegisteredDescriptorEntity::getId)
            .orElseThrow(() -> new AlertRuntimeException(String.format("No descriptor with name '%s' exists", descriptorName)));
    }
}
