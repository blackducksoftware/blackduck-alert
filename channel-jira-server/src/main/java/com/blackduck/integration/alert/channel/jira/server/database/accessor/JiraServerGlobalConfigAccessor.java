/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.database.accessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.util.AccessorLimitedMap;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.common.util.SortUtil;

@Component
public class JiraServerGlobalConfigAccessor implements ConfigurationAccessor<JiraServerGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final JiraServerConfigurationRepository jiraServerConfigurationRepository;

    private final AccessorLimitedMap<UUID, JiraServerGlobalConfigModel> globalConfigCache;

    @Autowired
    public JiraServerGlobalConfigAccessor(EncryptionUtility encryptionUtility, JiraServerConfigurationRepository jiraServerConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.jiraServerConfigurationRepository = jiraServerConfigurationRepository;
        this.globalConfigCache = new AccessorLimitedMap<>();
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return jiraServerConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraServerGlobalConfigModel> getConfiguration(UUID id) {
        if (globalConfigCache.containsKey(id)) {
            return Optional.of(globalConfigCache.get(id));
        }
        Optional<JiraServerGlobalConfigModel> model = jiraServerConfigurationRepository.findById(id).map(this::createConfigModel);

        model.ifPresent(jiraServerGlobalConfigModel -> globalConfigCache.put(id, jiraServerGlobalConfigModel));

        return model;
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
        return globalConfigCache.containsKey(id) || jiraServerConfigurationRepository.existsByConfigurationId(id);
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
        extractSavedFieldFromEntity(
            configuration::getIsPasswordSet,
            configuration::getPassword,
            configurationEntity::getPassword,
            configuration::setPassword
        );
        extractSavedFieldFromEntity(
            configuration::getIsAccessTokenSet,
            configuration::getAccessToken,
            configurationEntity::getAccessToken,
            configuration::setAccessToken
        );
        return populateConfiguration(configurationId, configuration, configurationEntity.getCreatedAt());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            jiraServerConfigurationRepository.deleteById(configurationId);
            globalConfigCache.remove(configurationId);
        }
    }

    private JiraServerGlobalConfigModel populateConfiguration(UUID configurationId, JiraServerGlobalConfigModel configuration, OffsetDateTime createdAt) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        removeUnusedAuthCredentials(configuration);
        JiraServerConfigurationEntity configurationToSave = toEntity(configurationId, configuration, createdAt, currentTime);
        JiraServerConfigurationEntity savedJiraServerConfig = jiraServerConfigurationRepository.save(configurationToSave);
        JiraServerGlobalConfigModel model = createConfigModel(savedJiraServerConfig);

        globalConfigCache.put(configurationId, model);

        return model;
    }

    private JiraServerConfigurationEntity toEntity(UUID configurationId, JiraServerGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        String password = configuration.getPassword().map(encryptionUtility::encrypt).orElse(null);
        String accessToken = configuration.getAccessToken().map(encryptionUtility::encrypt).orElse(null);
        Boolean disablePluginCheck = configuration.getDisablePluginCheck().orElse(Boolean.FALSE);

        return new JiraServerConfigurationEntity(
            configurationId,
            configuration.getName(),
            createdTime,
            lastUpdated,
            configuration.getUrl(),
            configuration.getTimeout().orElse(JiraServerPropertiesFactory.DEFAULT_JIRA_TIMEOUT_SECONDS),
            configuration.getAuthorizationMethod(),
            configuration.getUserName().orElse(null),
            password,
            accessToken,
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
        Integer timeout = jiraConfiguration.getTimeout();
        String username = jiraConfiguration.getUsername();
        String password = jiraConfiguration.getPassword();
        String accessToken = jiraConfiguration.getAccessToken();
        Boolean disablePluginCheck = jiraConfiguration.getDisablePluginCheck();

        boolean doesPasswordExist = StringUtils.isNotBlank(password);
        if (doesPasswordExist) {
            password = encryptionUtility.decrypt(password);
        }
        boolean doesAccessTokenExist = StringUtils.isNotBlank(accessToken);
        if (doesAccessTokenExist) {
            accessToken = encryptionUtility.decrypt(accessToken);
        }
        return new JiraServerGlobalConfigModel(
            id,
            name,
            createdAtFormatted,
            lastUpdatedFormatted,
            url,
            timeout,
            jiraConfiguration.getAuthorizationMethod(),
            username,
            password,
            doesPasswordExist,
            accessToken,
            doesAccessTokenExist,
            disablePluginCheck
        );
    }

    private void extractSavedFieldFromEntity(
        Supplier<Optional<Boolean>> isSetFieldSupplier,
        Supplier<Optional<String>> getFieldSupplier,
        Supplier<String> getEntitySupplier,
        Consumer<String> setFieldSupplier
    ) {
        if (BooleanUtils.toBoolean(isSetFieldSupplier.get().orElse(Boolean.FALSE)) && getFieldSupplier.get().isEmpty()) {
            String decryptedField = encryptionUtility.decrypt(getEntitySupplier.get());
            setFieldSupplier.accept(decryptedField);
        }
    }

    /**
     * Persist only the credentials used by the AuthorizationMethod. Switching between authorization methods will require users to reauthenticate their credentials.
     * @param configuration - The model passed in when creating or updating the configuration
     */
    private void removeUnusedAuthCredentials(JiraServerGlobalConfigModel configuration) {
        if (configuration.getAuthorizationMethod() == JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN) {
            configuration.setUserName(null);
            configuration.setPassword(null);
            configuration.setIsPasswordSet(null);
        } else {
            configuration.setAccessToken(null);
            configuration.setIsAccessTokenSet(null);
        }
    }
}
