package com.synopsys.integration.alert.channel.github.database.accessor;

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
import com.synopsys.integration.alert.channel.github.database.configuration.GitHubConfigurationEntity;
import com.synopsys.integration.alert.channel.github.database.configuration.GitHubConfigurationRepository;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.util.SortUtil;

@Component
public class GitHubGlobalConfigAccessor implements ConfigurationAccessor<GitHubGlobalConfigModel> {
    private final EncryptionUtility encryptionUtility;
    private final GitHubConfigurationRepository gitHubConfigurationRepository;

    @Autowired
    public GitHubGlobalConfigAccessor(EncryptionUtility encryptionUtility, GitHubConfigurationRepository gitHubConfigurationRepository) {
        this.encryptionUtility = encryptionUtility;
        this.gitHubConfigurationRepository = gitHubConfigurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCount() {
        return gitHubConfigurationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GitHubGlobalConfigModel> getConfiguration(UUID id) {
        return gitHubConfigurationRepository.findById(id).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GitHubGlobalConfigModel> getConfigurationByName(String configurationName) {
        return gitHubConfigurationRepository.findByName(configurationName).map(this::createConfigModel);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationByName(String configurationName) {
        return gitHubConfigurationRepository.existsByName(configurationName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsConfigurationById(UUID id) {
        return gitHubConfigurationRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<GitHubGlobalConfigModel> getConfigurationPage(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        Sort sort = SortUtil.createSortByFieldName(sortName, sortOrder);
        Page<GitHubConfigurationEntity> resultPage;
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        if (StringUtils.isNotBlank(searchTerm)) {
            resultPage = gitHubConfigurationRepository.findBySearchTerm(searchTerm, pageRequest);
        } else {
            resultPage = gitHubConfigurationRepository.findAll(pageRequest);
        }
        List<GitHubGlobalConfigModel> pageContent = resultPage.getContent()
            .stream()
            .map(this::createConfigModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(resultPage.getTotalPages(), resultPage.getNumber(), resultPage.getSize(), pageContent);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public GitHubGlobalConfigModel createConfiguration(GitHubGlobalConfigModel configuration) throws AlertConfigurationException {
        if (gitHubConfigurationRepository.existsByName(configuration.getName())) {
            throw new AlertConfigurationException(String.format("A config with the name '%s' already exists.", configuration.getName()));
        }
        UUID configurationId = UUID.randomUUID();
        configuration.setId(configurationId.toString());
        return populateConfiguration(configurationId, configuration, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public GitHubGlobalConfigModel updateConfiguration(UUID configurationId, GitHubGlobalConfigModel configuration) throws AlertConfigurationException {
        GitHubConfigurationEntity configurationEntity = gitHubConfigurationRepository.findById(configurationId)
            .orElseThrow(() -> new AlertConfigurationException(String.format("Config with id '%s' did not exist", configurationId)));
        if (BooleanUtils.toBoolean(configuration.getIsApiTokenSet().orElse(Boolean.FALSE)) && configuration.getApiToken().isEmpty()) {
            String decryptedPassword = encryptionUtility.decrypt(configurationEntity.getApiToken());
            configuration.setApiToken(decryptedPassword);
        }
        return populateConfiguration(configurationId, configuration, configurationEntity.getCreatedAt());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteConfiguration(UUID configurationId) {
        if (null != configurationId) {
            gitHubConfigurationRepository.deleteById(configurationId);
        }
    }

    private GitHubGlobalConfigModel populateConfiguration(UUID configurationId, GitHubGlobalConfigModel configuration, OffsetDateTime createdAt) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        GitHubConfigurationEntity configurationToSave = toEntity(configurationId, configuration, createdAt, currentTime);
        GitHubConfigurationEntity savedGitHubConfig = gitHubConfigurationRepository.save(configurationToSave);
        return createConfigModel(savedGitHubConfig);
    }

    private GitHubConfigurationEntity toEntity(UUID configurationId, GitHubGlobalConfigModel configuration, OffsetDateTime createdTime, OffsetDateTime lastUpdated) {
        return new GitHubConfigurationEntity(
            configurationId,
            configuration.getName(),
            createdTime,
            lastUpdated,
            encryptionUtility.encrypt(configuration.getApiToken()),
            configuration.getTimeoutInSeconds()
        );
    }

    private GitHubGlobalConfigModel createConfigModel(GitHubConfigurationEntity gitHubConfiguration) {
        String createdAtFormatted = DateUtils.formatDate(gitHubConfiguration.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String lastUpdatedFormatted = "";
        if (null != gitHubConfiguration.getLastUpdated()) {
            lastUpdatedFormatted = DateUtils.formatDate(gitHubConfiguration.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        }
        String id = String.valueOf(gitHubConfiguration.getConfigurationId());
        String name = gitHubConfiguration.getName();
        Long timeoutInSeconds = gitHubConfiguration.getTimeoutSeconds();

        String apiToken = gitHubConfiguration.getApiToken();
        boolean isApiTokenSet = StringUtils.isNotBlank(apiToken);
        if (isApiTokenSet) {
            apiToken = encryptionUtility.decrypt(apiToken);
        }

        return new GitHubGlobalConfigModel(id, name, createdAtFormatted, lastUpdatedFormatted, apiToken, isApiTokenSet, timeoutInSeconds);
    }
}
