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
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;

@Component
public class SettingsProxyConfigAccessor implements ConfigurationAccessor<SettingsProxyModel> {
    private final EncryptionUtility encryptionUtility;
    private final SettingsProxyConfigurationRepository settingsProxyConfigurationRepository;

    public SettingsProxyConfigAccessor(
        EncryptionUtility encryptionUtility,
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository
    ) {
        this.encryptionUtility = encryptionUtility;
        this.settingsProxyConfigurationRepository = settingsProxyConfigurationRepository;
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
        SettingsProxyConfigurationEntity configurationToSave = toEntity(configuration, currentTime, currentTime);
        SettingsProxyConfigurationEntity savedProxyConfig = settingsProxyConfigurationRepository.save(configurationToSave);

        return createConfigModel(savedProxyConfig);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SettingsProxyModel updateConfiguration(UUID configurationId, SettingsProxyModel configuration) throws AlertConfigurationException {
        SettingsProxyConfigurationEntity configurationEntity = settingsProxyConfigurationRepository.findById(configurationId)
                                                                   .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%d' did not exist", configurationId)));
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        SettingsProxyConfigurationEntity configurationToSave = toEntity(configuration, configurationEntity.getCreatedAt(), currentTime);
        SettingsProxyConfigurationEntity savedProxyConfig = settingsProxyConfigurationRepository.save(configurationToSave);

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
        String createdAtFormatted = "";
        String lastUpdatedFormatted = "";
        if (null != proxyConfiguration) {
            createdAtFormatted = DateUtils.formatDate(proxyConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            if (null != proxyConfiguration.getLastUpdated()) {
                lastUpdatedFormatted = DateUtils.formatDate(proxyConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
            }
            newModel.setHost(proxyConfiguration.getHost());
            newModel.setPort(proxyConfiguration.getPort());
            newModel.setUsername(proxyConfiguration.getUsername());
            newModel.setNonProxyHosts(proxyConfiguration.getNonProxyHosts());
            if (StringUtils.isNotBlank(proxyConfiguration.getPassword())) {
                newModel.setPassword(encryptionUtility.decrypt(proxyConfiguration.getPassword()));
            }
        }
        newModel.setId(String.valueOf(proxyConfiguration.getConfigurationId()));
        newModel.setCreatedAt(createdAtFormatted);
        newModel.setLastUpdated(lastUpdatedFormatted);

        return newModel;
    }

    private SettingsProxyConfigurationEntity toEntity(SettingsProxyModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        UUID configurationId = null;
        if (StringUtils.isNotBlank(configuration.getId())) {
            configurationId = UUID.fromString(configuration.getId());
        }
        String host = configuration.getHost().orElseThrow(null);
        Integer port = configuration.getPort().orElse(null);
        String username = configuration.getUsername().orElse(null);
        String password = configuration.getPassword().map(encryptionUtility::encrypt).orElse(null);
        String nonProxyHosts = configuration.getNonProxyHosts().orElse(null);

        return new SettingsProxyConfigurationEntity(configurationId, createdTime, lastUpdated, host, port, username, password, nonProxyHosts);
    }
}
