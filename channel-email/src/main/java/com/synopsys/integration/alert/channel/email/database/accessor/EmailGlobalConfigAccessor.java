/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.database.accessor;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.database.configuration.EmailConfigurationEntity;
import com.synopsys.integration.alert.channel.email.database.configuration.EmailConfigurationRepository;
import com.synopsys.integration.alert.channel.email.database.configuration.properties.EmailConfigurationPropertiesRepository;
import com.synopsys.integration.alert.channel.email.database.configuration.properties.EmailConfigurationsPropertyEntity;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigAccessor implements UniqueConfigurationAccessor<EmailGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final EmailConfigurationRepository emailConfigurationRepository;
    private final EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository;

    @Autowired
    public EmailGlobalConfigAccessor(
        EncryptionUtility encryptionUtility,
        EmailConfigurationRepository emailConfigurationRepository,
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository
    ) {
        this.encryptionUtility = encryptionUtility;
        this.emailConfigurationRepository = emailConfigurationRepository;
        this.emailConfigurationPropertiesRepository = emailConfigurationPropertiesRepository;
    }

    @Transactional(readOnly = true)
    public boolean doesConfigurationExist() {
        return emailConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailGlobalConfigModel> getConfiguration() {
        return emailConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::createConfigModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EmailGlobalConfigModel createConfiguration(EmailGlobalConfigModel configuration) throws AlertConfigurationException {
        if (emailConfigurationRepository.existsByName(configuration.getName())) {
            throw new AlertConfigurationException(String.format("A config with the name '%s' already exists.", configuration.getName()));
        }
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        return populateConfiguration(configurationId, configuration, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EmailGlobalConfigModel updateConfiguration(EmailGlobalConfigModel configuration) throws AlertConfigurationException {
        EmailConfigurationEntity configurationEntity = emailConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with name '%s' did not exist", AlertRestConstants.DEFAULT_CONFIGURATION_NAME)));
        if (BooleanUtils.toBoolean(configuration.getIsSmtpPasswordSet()) && configuration.getSmtpPassword().isEmpty()) {
            String decryptedPassword = encryptionUtility.decrypt(configurationEntity.getAuthPassword());
            configuration.setSmtpPassword(decryptedPassword);
        }
        return populateConfiguration(configurationEntity.getConfigurationId(), configuration, configurationEntity.getCreatedAt());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        emailConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private EmailGlobalConfigModel populateConfiguration(UUID configurationId, EmailGlobalConfigModel configuration, OffsetDateTime createdAt) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        EmailConfigurationEntity configurationToSave = toEntity(configurationId, configuration, createdAt, currentTime);
        EmailConfigurationEntity savedEmailConfig = emailConfigurationRepository.save(configurationToSave);
        emailConfigurationPropertiesRepository.bulkDeleteByConfigurationId(savedEmailConfig.getConfigurationId());
        List<EmailConfigurationsPropertyEntity> emailProperties = toPropertyEntityList(configurationId, configuration);
        emailConfigurationPropertiesRepository.saveAll(emailProperties);
        savedEmailConfig = emailConfigurationRepository.getOne(savedEmailConfig.getConfigurationId());

        return createConfigModel(savedEmailConfig);
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
            newModel.setId(String.valueOf(emailConfiguration.getConfigurationId()));
            newModel.setName(emailConfiguration.getName());
            newModel.setSmtpHost(emailConfiguration.getSmtpHost());
            newModel.setSmtpPort(emailConfiguration.getSmtpPort());
            newModel.setSmtpFrom(emailConfiguration.getSmtpFrom());
            newModel.setSmtpAuth(emailConfiguration.getAuthRequired());
            newModel.setSmtpUsername(emailConfiguration.getAuthUsername());
            String authPassword = emailConfiguration.getAuthPassword();
            boolean doesPasswordExist = StringUtils.isNotBlank(authPassword);
            newModel.setIsSmtpPasswordSet(doesPasswordExist);
            if (doesPasswordExist) {
                newModel.setSmtpPassword(encryptionUtility.decrypt(authPassword));
            }
            newModel.setAdditionalJavaMailProperties(getAdditionalProperties(emailConfiguration.getEmailConfigurationProperties()));
        }
        newModel.setCreatedAt(createdAtFormatted);
        newModel.setLastUpdated(lastUpdatedFormatted);

        return newModel;
    }

    private Map<String, String> getAdditionalProperties(List<EmailConfigurationsPropertyEntity> emailConfigurationProperties) {
        return emailConfigurationProperties.stream()
            .collect(Collectors.toMap(EmailConfigurationsPropertyEntity::getPropertyKey, EmailConfigurationsPropertyEntity::getPropertyValue));
    }

    private EmailConfigurationEntity toEntity(UUID configurationId, EmailGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        String host = configuration.getSmtpHost().orElse(null);
        String from = configuration.getSmtpFrom().orElse(null);
        Integer port = configuration.getSmtpPort().orElse(null);
        Boolean auth = configuration.getSmtpAuth().orElse(Boolean.FALSE);
        String username = configuration.getSmtpUsername().orElse(null);
        String password = configuration.getSmtpPassword().map(encryptionUtility::encrypt).orElse(null);

        return new EmailConfigurationEntity(configurationId, configuration.getName(), createdTime, lastUpdated,
            host, from, port, auth, username, password, List.of());
    }

    private List<EmailConfigurationsPropertyEntity> toPropertyEntityList(UUID configurationId, EmailGlobalConfigModel configuration) {
        Map<String, String> emailProperties = configuration.getAdditionalJavaMailProperties().orElse(Map.of());
        List<EmailConfigurationsPropertyEntity> propertyList = new LinkedList<>();
        for (Map.Entry<String, String> entry : emailProperties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isNotBlank(key)) {
                propertyList.add(new EmailConfigurationsPropertyEntity(configurationId, key.trim(), value.trim()));
            }
        }

        return propertyList;
    }
}
