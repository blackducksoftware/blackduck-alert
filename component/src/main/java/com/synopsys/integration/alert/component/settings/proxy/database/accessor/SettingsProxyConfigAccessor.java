/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.proxy.database.accessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.UniqueConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;

@Component
public class SettingsProxyConfigAccessor implements UniqueConfigurationAccessor<SettingsProxyModel> {
    private final EncryptionUtility encryptionUtility;
    private final SettingsProxyConfigurationRepository settingsProxyConfigurationRepository;
    private final NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository;

    public SettingsProxyConfigAccessor(
        EncryptionUtility encryptionUtility,
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository,
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository
    ) {
        this.encryptionUtility = encryptionUtility;
        this.settingsProxyConfigurationRepository = settingsProxyConfigurationRepository;
        this.nonProxyHostsConfigurationRepository = nonProxyHostsConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SettingsProxyModel> getConfiguration() {
        return settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME).map(this::createConfigModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SettingsProxyModel createConfiguration(SettingsProxyModel configuration) throws AlertConfigurationException {
        Optional<SettingsProxyModel> existingConfiguration = getConfiguration();
        if (existingConfiguration.isPresent()) {
            throw new AlertConfigurationException("A proxy config already exists.");
        }

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        SettingsProxyConfigurationEntity configurationToSave = toEntity(configurationId, configuration, currentTime, currentTime);
        SettingsProxyConfigurationEntity savedProxyConfig = settingsProxyConfigurationRepository.save(configurationToSave);
        List<NonProxyHostConfigurationEntity> nonProxyHosts = toNonProxyHostEntityList(configurationId, configuration);
        nonProxyHostsConfigurationRepository.saveAll(nonProxyHosts);
        savedProxyConfig = settingsProxyConfigurationRepository.getOne(savedProxyConfig.getConfigurationId());

        return createConfigModel(savedProxyConfig);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SettingsProxyModel updateConfiguration(SettingsProxyModel configuration) throws AlertConfigurationException {
        SettingsProxyConfigurationEntity configurationEntity = settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                                                                   .orElseThrow(() -> new AlertConfigurationException("Proxy config does not exist"));

        if (BooleanUtils.toBoolean(configuration.getIsProxyPasswordSet()) && configuration.getProxyPassword().isEmpty()) {
            String decryptedPassword = encryptionUtility.decrypt(configurationEntity.getPassword());
            configuration.setProxyPassword(decryptedPassword);
        }
        UUID configurationId = configurationEntity.getConfigurationId();
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        SettingsProxyConfigurationEntity configurationToSave = toEntity(configurationId, configuration, configurationEntity.getCreatedAt(), currentTime);
        SettingsProxyConfigurationEntity savedProxyConfig = settingsProxyConfigurationRepository.save(configurationToSave);
        List<NonProxyHostConfigurationEntity> nonProxyHosts = toNonProxyHostEntityList(configurationId, configuration);
        nonProxyHostsConfigurationRepository.saveAll(nonProxyHosts);
        savedProxyConfig = settingsProxyConfigurationRepository.getOne(savedProxyConfig.getConfigurationId());

        return createConfigModel(savedProxyConfig);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration() {
        settingsProxyConfigurationRepository.deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
    }

    private SettingsProxyModel createConfigModel(SettingsProxyConfigurationEntity proxyConfiguration) {
        SettingsProxyModel newModel = new SettingsProxyModel();
        String createdAtFormatted = null;
        String lastUpdatedFormatted = null;
        if (null != proxyConfiguration) {
            createdAtFormatted = DateUtils.formatDate(proxyConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            if (null != proxyConfiguration.getLastUpdated()) {
                lastUpdatedFormatted = DateUtils.formatDate(proxyConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            }
            newModel.setId(String.valueOf(proxyConfiguration.getConfigurationId()));
            newModel.setName(proxyConfiguration.getName());
            newModel.setProxyHost(proxyConfiguration.getHost());
            newModel.setProxyPort(proxyConfiguration.getPort());
            newModel.setProxyUsername(proxyConfiguration.getUsername());

            String proxyPassword = proxyConfiguration.getPassword();
            boolean doesPasswordExist = StringUtils.isNotBlank(proxyPassword);
            newModel.setIsProxyPasswordSet(doesPasswordExist);
            if (doesPasswordExist) {
                newModel.setProxyPassword(encryptionUtility.decrypt(proxyPassword));
            }
            newModel.setNonProxyHosts(getNonProxyHosts(proxyConfiguration.getNonProxyHosts()));
        }
        newModel.setCreatedAt(createdAtFormatted);
        newModel.setLastUpdated(lastUpdatedFormatted);

        return newModel;
    }

    private List<String> getNonProxyHosts(List<NonProxyHostConfigurationEntity> nonProxyHosts) {
        return nonProxyHosts
                   .stream()
                   .map(NonProxyHostConfigurationEntity::getHostnamePattern)
                   .collect(Collectors.toList());
    }

    private SettingsProxyConfigurationEntity toEntity(UUID configurationId, SettingsProxyModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        String host = configuration.getProxyHost().orElse(null);
        Integer port = configuration.getProxyPort().orElse(null);
        String username = configuration.getProxyUsername().orElse(null);
        String password = configuration.getProxyPassword().map(encryptionUtility::encrypt).orElse(null);

        return new SettingsProxyConfigurationEntity(configurationId, configuration.getName(), createdTime, lastUpdated, host, port, username, password, List.of());
    }

    private List<NonProxyHostConfigurationEntity> toNonProxyHostEntityList(UUID configurationId, SettingsProxyModel configuration) {
        return configuration.getNonProxyHosts().orElse(List.of())
            .stream()
            .map(hostnamePattern -> new NonProxyHostConfigurationEntity(configurationId, hostnamePattern))
            .collect(Collectors.toList());
    }
}
