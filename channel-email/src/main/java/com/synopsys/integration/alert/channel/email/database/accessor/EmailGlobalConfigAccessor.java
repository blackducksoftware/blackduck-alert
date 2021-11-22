/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.email.EmailConfigurationEntity;
import com.synopsys.integration.alert.database.email.EmailConfigurationRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationPropertiesRepository;
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationsPropertyEntity;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigAccessor implements ConfigurationAccessor<EmailGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final EmailConfigurationRepository emailConfigurationRepository;
    private final EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository;

    @Autowired
    public EmailGlobalConfigAccessor(EncryptionUtility encryptionUtility,
        EmailConfigurationRepository emailConfigurationRepository,
        EmailConfigurationPropertiesRepository emailConfigurationPropertiesRepository
    ) {
        this.encryptionUtility = encryptionUtility;
        this.emailConfigurationRepository = emailConfigurationRepository;
        this.emailConfigurationPropertiesRepository = emailConfigurationPropertiesRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return emailConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailGlobalConfigModel> getConfiguration(UUID id) {
        return emailConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<EmailGlobalConfigModel> getConfigurationPage(int page, int size) {
        Page<EmailConfigurationEntity> resultPage = emailConfigurationRepository.findAll(PageRequest.of(page, size));
        List<EmailGlobalConfigModel> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
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
    @Transactional(propagation = Propagation.REQUIRED)
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
            if (StringUtils.isNotBlank(emailConfiguration.getAuthPassword())) {
                newModel.setPassword(encryptionUtility.decrypt(emailConfiguration.getAuthPassword()));
            }
            newModel.setAdditionalJavaMailProperties(getAdditionalProperties(emailConfiguration.getEmailConfigurationProperties()));
        }

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
        String host = configuration.getHost().orElse(null);
        String from = configuration.getFrom().orElse(null);
        Integer port = configuration.getPort().orElse(null);
        Boolean auth = configuration.getAuth().orElse(Boolean.FALSE);
        String username = configuration.getUsername().orElse(null);
        String password = configuration.getPassword().map(encryptionUtility::encrypt).orElse(null);

        return new EmailConfigurationEntity(configurationId, createdTime, lastUpdated,
            host, from, port, auth, username, password, List.of());
    }

    private List<EmailConfigurationsPropertyEntity> toPropertyEntityList(EmailGlobalConfigModel configuration) {
        UUID configurationId = UUID.fromString(configuration.getId());
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
