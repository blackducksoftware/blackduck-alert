package com.synopsys.integration.alert.channel.azure.boards.database.accessor;

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
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.util.SortUtil;

@Component
public class AzureBoardsGlobalConfigAccessor implements ConfigurationAccessor<AzureBoardsGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final AzureBoardsConfigurationRepository azureBoardsConfigurationRepository;

    @Autowired
    public AzureBoardsGlobalConfigAccessor(EncryptionUtility encryptionUtility, AzureBoardsConfigurationRepository azureBoardsConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.azureBoardsConfigurationRepository = azureBoardsConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return azureBoardsConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AzureBoardsGlobalConfigModel> getConfiguration(UUID id) {
        return azureBoardsConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AzureBoardsGlobalConfigModel> getConfigurationByName(String configurationName) {
        return azureBoardsConfigurationRepository.findByName(configurationName).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationByName(String configurationName) {
        return azureBoardsConfigurationRepository.existsByName(configurationName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationById(UUID id) {
        return azureBoardsConfigurationRepository.existsByConfigurationId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<AzureBoardsGlobalConfigModel> getConfigurationPage(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        Sort sort = SortUtil.createSortByFieldName(sortName, sortOrder);
        Page<AzureBoardsConfigurationEntity> resultPage;
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        if (StringUtils.isNotBlank(searchTerm)) {
            resultPage = azureBoardsConfigurationRepository.findBySearchTerm(searchTerm, pageRequest);
        } else {
            resultPage = azureBoardsConfigurationRepository.findAll(pageRequest);
        }
        List<AzureBoardsGlobalConfigModel> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AzureBoardsGlobalConfigModel createConfiguration(AzureBoardsGlobalConfigModel configuration) throws AlertConfigurationException {
        if (azureBoardsConfigurationRepository.existsByName(configuration.getName())) {
            throw new AlertConfigurationException(String.format("A config with the name '%s' already exists.", configuration.getName()));
        }
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        return populateConfiguration(configurationId, configuration, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AzureBoardsGlobalConfigModel updateConfiguration(UUID configurationId, AzureBoardsGlobalConfigModel configuration) throws AlertConfigurationException {
        AzureBoardsConfigurationEntity configurationEntity = azureBoardsConfigurationRepository.findById(configurationId)
            .orElseThrow(() -> new AlertConfigurationException((String.format("Config with id '%s' did not exist", configurationId))));
        if (BooleanUtils.toBoolean(configuration.getIsAppIdSet().orElse(Boolean.FALSE)) && configuration.getAppId().isEmpty()) {
            String decryptedAppId = encryptionUtility.decrypt(configurationEntity.getAppId());
            configuration.setAppId(decryptedAppId);
        }
        if (BooleanUtils.toBoolean(configuration.getIsClientSecretSet().orElse(Boolean.FALSE)) && configuration.getClientSecret().isEmpty()) {
            String decryptedClientSecret = encryptionUtility.decrypt(configurationEntity.getClientSecret());
            configuration.setClientSecret(decryptedClientSecret);
        }
        return populateConfiguration(configurationId, configuration, configurationEntity.getCreatedAt());
    }

    @Override
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            azureBoardsConfigurationRepository.deleteById(configurationId);
        }
    }

    private AzureBoardsGlobalConfigModel populateConfiguration(UUID configurationId, AzureBoardsGlobalConfigModel configuration, OffsetDateTime createdAt) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        AzureBoardsConfigurationEntity configurationToSave = toEntity(configurationId, configuration, createdAt, currentTime);
        AzureBoardsConfigurationEntity savedAzureBoardsConfig = azureBoardsConfigurationRepository.save(configurationToSave);
        return createConfigModel(savedAzureBoardsConfig);
    }

    private AzureBoardsConfigurationEntity toEntity(UUID configurationId, AzureBoardsGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        String appId = configuration.getAppId().map(encryptionUtility::encrypt).orElse(null);
        String clientSecret = configuration.getClientSecret().map(encryptionUtility::encrypt).orElse(null);
        return new AzureBoardsConfigurationEntity(
            configurationId,
            configuration.getName(),
            createdTime,
            lastUpdated,
            configuration.getOrganizationName(),
            appId,
            clientSecret
        );
    }

    private AzureBoardsGlobalConfigModel createConfigModel(AzureBoardsConfigurationEntity azureBoardsConfiguration) {
        String createdAtFormatted = DateUtils.formatDate(azureBoardsConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = "";
        if (null != azureBoardsConfiguration.getLastUpdated()) {
            lastUpdatedFormatted = DateUtils.formatDate(azureBoardsConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        }
        String id = String.valueOf(azureBoardsConfiguration.getConfigurationId());
        String name = azureBoardsConfiguration.getName();
        String organizationName = azureBoardsConfiguration.getOrganizationName();
        String appId = azureBoardsConfiguration.getAppId();
        String clientSecret = azureBoardsConfiguration.getClientSecret();

        boolean doesAppIdExist = StringUtils.isNotBlank(appId);
        if (doesAppIdExist) {
            appId = encryptionUtility.decrypt(appId);
        }
        boolean doesClientSecretExist = StringUtils.isNotBlank(clientSecret);
        if (doesClientSecretExist) {
            clientSecret = encryptionUtility.decrypt(clientSecret);
        }
        return new AzureBoardsGlobalConfigModel(id, name, createdAtFormatted, lastUpdatedFormatted, organizationName, appId, doesAppIdExist, clientSecret, doesClientSecretExist);
    }
}
