/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.database.accessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.util.SortUtil;

@Component
public class JiraServerGlobalConfigAccessor implements ConfigurationAccessor<JiraServerGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final JiraServerConfigurationRepository jiraServerConfigurationRepository;

    @Autowired
    public JiraServerGlobalConfigAccessor(EncryptionUtility encryptionUtility, JiraServerConfigurationRepository jiraServerConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.jiraServerConfigurationRepository = jiraServerConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return jiraServerConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraServerGlobalConfigModel> getConfiguration(UUID id) {
        return jiraServerConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraServerGlobalConfigModel> getConfigurationByName(String configurationName) {
        return jiraServerConfigurationRepository.findByName(configurationName).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationByName(String configurationName) {
        return jiraServerConfigurationRepository.existsByName(configurationName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationById(UUID id) {
        return jiraServerConfigurationRepository.existsByConfigurationId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<JiraServerGlobalConfigModel> getConfigurationPage(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        Sort sort = SortUtil.createSortByFieldName(sortName, sortOrder);
        Page<JiraServerConfigurationEntity> resultPage;
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        if (StringUtils.isNotBlank(searchTerm)) {
            resultPage = jiraServerConfigurationRepository.findBySearchTerm(searchTerm, pageRequest);
        } else {
            resultPage = jiraServerConfigurationRepository.findAll(pageRequest);
        }
        List<JiraServerGlobalConfigModel> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerGlobalConfigModel createConfiguration(JiraServerGlobalConfigModel configuration) throws AlertConfigurationException {
        if (jiraServerConfigurationRepository.existsByName(configuration.getName())) {
            throw new AlertConfigurationException(String.format("A config with the name '%s' already exists.", configuration.getName()));
        }
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        return populateConfiguration(configurationId, configuration, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerGlobalConfigModel updateConfiguration(UUID configurationId, JiraServerGlobalConfigModel configuration) throws AlertConfigurationException {
        JiraServerConfigurationEntity configurationEntity = jiraServerConfigurationRepository.findById(configurationId)
            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%s' did not exist", configurationId.toString())));
        if (BooleanUtils.toBoolean(configuration.getIsPasswordSet().orElse(Boolean.FALSE)) && configuration.getPassword().isEmpty()) {
            String decryptedPassword = encryptionUtility.decrypt(configurationEntity.getPassword());
            configuration.setPassword(decryptedPassword);
        }
        return populateConfiguration(configurationId, configuration, configurationEntity.getCreatedAt());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            jiraServerConfigurationRepository.deleteById(configurationId);
        }
    }

    private JiraServerGlobalConfigModel populateConfiguration(UUID configurationId, JiraServerGlobalConfigModel configuration, OffsetDateTime createdAt) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        JiraServerConfigurationEntity configurationToSave = toEntity(configurationId, configuration, createdAt, currentTime);
        JiraServerConfigurationEntity savedJiraServerConfig = jiraServerConfigurationRepository.save(configurationToSave);
        return createConfigModel(savedJiraServerConfig);
    }

    private JiraServerConfigurationEntity toEntity(UUID configurationId, JiraServerGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        String password = configuration.getPassword().map(encryptionUtility::encrypt).orElse(null);
        Boolean disablePluginCheck = configuration.getDisablePluginCheck().orElse(Boolean.FALSE);

        return new JiraServerConfigurationEntity(
            configurationId,
            configuration.getName(),
            createdTime,
            lastUpdated,
            configuration.getUrl(),
            configuration.getAuthorizationMethod(),
            configuration.getUserName().orElse(null),
            password,
            disablePluginCheck
        );
    }

    private JiraServerGlobalConfigModel createConfigModel(JiraServerConfigurationEntity jiraConfiguration) {
        String createdAtFormatted = DateUtils.formatDate(jiraConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = "";
        if (null != jiraConfiguration.getLastUpdated()) {
            lastUpdatedFormatted = DateUtils.formatDate(jiraConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        }
        String id = String.valueOf(jiraConfiguration.getConfigurationId());
        String name = jiraConfiguration.getName();
        String url = jiraConfiguration.getUrl();
        String username = jiraConfiguration.getUsername();
        String password = jiraConfiguration.getPassword();
        Boolean disablePluginCheck = jiraConfiguration.getDisablePluginCheck();

        boolean doesPasswordExist = StringUtils.isNotBlank(password);
        if (doesPasswordExist) {
            password = encryptionUtility.decrypt(password);
        }
        // TODO: Implement access token and AuthorizationMethod
        return new JiraServerGlobalConfigModel(
            id,
            name,
            createdAtFormatted,
            lastUpdatedFormatted,
            url,
            JiraServerAuthorizationMethod.BASIC,
            username,
            password,
            doesPasswordExist,
            null,
            null,
            disablePluginCheck
        );
    }
}
