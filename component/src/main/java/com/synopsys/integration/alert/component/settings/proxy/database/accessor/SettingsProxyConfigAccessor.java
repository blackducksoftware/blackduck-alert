/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.proxy.database.accessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
import com.synopsys.integration.alert.component.settings.proxy.model.SettingsProxyModel;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;

@Component
public class SettingsProxyConfigAccessor implements ConfigurationAccessor<SettingsProxyModel> {
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
    public long getConfigurationCount() {
        return settingsProxyConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SettingsProxyModel> getConfiguration(UUID id) {
        return settingsProxyConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<SettingsProxyModel> getConfigurationPage(int page, int size) {
        Page<SettingsProxyConfigurationEntity> resultPage = settingsProxyConfigurationRepository.findAll(PageRequest.of(page, size));
        List<SettingsProxyModel> pageContent = resultPage.getContent()
                                                   .stream()
                                                   .map(this::createConfigModel)
                                                   .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SettingsProxyModel createConfiguration(SettingsProxyModel configuration) {
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
    public SettingsProxyModel updateConfiguration(UUID configurationId, SettingsProxyModel configuration) throws AlertConfigurationException {
        SettingsProxyConfigurationEntity configurationEntity = settingsProxyConfigurationRepository.findById(configurationId)
                                                                   .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%d' did not exist", configurationId)));
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
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            settingsProxyConfigurationRepository.deleteById(configurationId);
        }
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
            newModel.setProxyHost(proxyConfiguration.getHost());
            newModel.setProxyPort(proxyConfiguration.getPort());
            newModel.setProxyUsername(proxyConfiguration.getUsername());
            if (StringUtils.isNotBlank(proxyConfiguration.getPassword())) {
                newModel.setProxyPassword(encryptionUtility.decrypt(proxyConfiguration.getPassword()));
            }
            newModel.setNonProxyHosts(getNonProxyHosts(proxyConfiguration.getNonProxyHosts()));
        }
        newModel.setId(String.valueOf(proxyConfiguration.getConfigurationId()));
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
        String host = configuration.getProxyHost().orElseThrow(null);
        Integer port = configuration.getProxyPort().orElse(null);
        String username = configuration.getProxyUsername().orElse(null);
        String password = configuration.getProxyPassword().map(encryptionUtility::encrypt).orElse(null);

        return new SettingsProxyConfigurationEntity(configurationId, createdTime, lastUpdated, host, port, username, password, List.of());
    }

    private List<NonProxyHostConfigurationEntity> toNonProxyHostEntityList(UUID configurationId, SettingsProxyModel configuration) {
        return configuration.getNonProxyHosts().orElse(List.of())
            .stream()
            .map(hostnamePattern -> new NonProxyHostConfigurationEntity(configurationId, hostnamePattern))
            .collect(Collectors.toList());
    }
}
