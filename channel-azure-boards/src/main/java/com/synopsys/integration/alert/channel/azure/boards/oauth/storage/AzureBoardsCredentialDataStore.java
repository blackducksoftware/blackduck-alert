/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

/**
 * This class is meant to be a very basic implementation of a {@link DataStore}.<br/>
 * It allows leveraging auto-refresh of access-tokens and general Google API goodies.<br/>
 * If OAuth is to be expanded beyond storing a single credential for a single user, <br/>
 * this implementation will have to be updated to not be specific to Azure Boards.
 */
public class AzureBoardsCredentialDataStore extends AbstractDataStore<StoredCredential> {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    /**
     * @param dataStoreFactory      data store factory
     * @param id                    data store ID
     * @param configurationModelConfigurationAccessor Alert interface to read/write to descriptor configurations
     */
    protected AzureBoardsCredentialDataStore(AzureBoardsCredentialDataStoreFactory dataStoreFactory, String id, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        super(dataStoreFactory, id);
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public Set<String> keySet() throws IOException {
        ConfigurationModel configurationModel = retrieveConfiguration();
        return configurationModel.getField(AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL)
                   .flatMap(ConfigurationFieldModel::getFieldValue)
                   .map(Set::of)
                   .orElse(Set.of());
    }

    @Override
    public Collection<StoredCredential> values() throws IOException {
        ConfigurationModel configurationModel = retrieveConfiguration();
        StoredCredential storedCredential = createStoredCredential(configurationModel);
        return Set.of(storedCredential);
    }

    @Override
    public StoredCredential get(String key) throws IOException {
        if (null == key) {
            return null;
        }
        return retrieveCredentialMatchingEmailOrNull(key);
    }

    @Override
    public AzureBoardsCredentialDataStore set(String key, StoredCredential value) throws IOException {
        if (null != key && null != value) {
            ConfigurationModel defaultConfig = retrieveConfiguration();
            Map<String, ConfigurationFieldModel> keyToFieldMap = defaultConfig.getCopyOfKeyToFieldMap();
            setFieldValue(keyToFieldMap, AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL, key);
            setFieldValue(keyToFieldMap, AzureBoardsDescriptor.KEY_ACCESS_TOKEN, value.getAccessToken());
            setFieldValue(keyToFieldMap, AzureBoardsDescriptor.KEY_REFRESH_TOKEN, value.getRefreshToken());

            Long expTimeMillis = value.getExpirationTimeMilliseconds();
            String expTimeMillisString = expTimeMillis != null ? expTimeMillis.toString() : null;
            setFieldValue(keyToFieldMap, AzureBoardsDescriptor.KEY_TOKEN_EXPIRATION_MILLIS, expTimeMillisString);

            try {
                configurationModelConfigurationAccessor.updateConfiguration(defaultConfig.getConfigurationId(), keyToFieldMap.values());
            } catch (AlertConfigurationException e) {
                throw new IOException("Cannot update the Azure Boards global configuration", e);
            }
        }
        return this;
    }

    private void setFieldValue(Map<String, ConfigurationFieldModel> keyToFieldMap, String fieldKey, String value) {
        ConfigurationFieldModel configurationFieldModel = keyToFieldMap.computeIfAbsent(fieldKey, ignored -> ConfigurationFieldModel.create(fieldKey));
        configurationFieldModel.setFieldValue(value);
    }

    @Override
    public AzureBoardsCredentialDataStore clear() throws IOException {
        for (String key : keySet()) {
            delete(key);
        }
        return this;
    }

    @Override
    public AzureBoardsCredentialDataStore delete(String key) {
        if (null != key) {
            ConfigurationModel defaultConfig = retrieveConfiguration();
            if (isConfiguredWithUserEmail(defaultConfig, key)) {
                configurationModelConfigurationAccessor.deleteConfiguration(defaultConfig.getConfigurationId());
            }
        }
        return this;
    }

    private StoredCredential retrieveCredentialMatchingEmailOrNull(String credentialUserEmail) {
        ConfigurationModel defaultConfig = retrieveConfiguration();
        if (isConfiguredWithUserEmail(defaultConfig, credentialUserEmail)) {
            return createStoredCredential(defaultConfig);
        }
        return null;
    }

    private ConfigurationModel retrieveConfiguration() {
        return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKeys.AZURE_BOARDS, ConfigContextEnum.GLOBAL)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertRuntimeException("No Azure Boards global configuration exists. Cannot read data store."));
    }

    private boolean isConfiguredWithUserEmail(ConfigurationModel configurationModel, String credentialUserEmail) {
        return configurationModel.getField(AzureBoardsDescriptor.KEY_OAUTH_USER_EMAIL)
                   .flatMap(ConfigurationFieldModel::getFieldValue)
                   .filter(credentialUserEmail::equals)
                   .isPresent();
    }

    private StoredCredential createStoredCredential(ConfigurationModel configurationModel) {
        String accessToken = getNullableValue(configurationModel, AzureBoardsDescriptor.KEY_ACCESS_TOKEN);
        String refreshToken = getNullableValue(configurationModel, AzureBoardsDescriptor.KEY_REFRESH_TOKEN);

        String tokenExpirationMillisString = getNullableValue(configurationModel, AzureBoardsDescriptor.KEY_TOKEN_EXPIRATION_MILLIS);
        Long tokenExpirationMillis = StringUtils.isNotBlank(tokenExpirationMillisString) ? Long.parseLong(tokenExpirationMillisString) : null;

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setAccessToken(accessToken);
        storedCredential.setRefreshToken(refreshToken);
        storedCredential.setExpirationTimeMilliseconds(tokenExpirationMillis);
        return storedCredential;
    }

    private String getNullableValue(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey)
                   .flatMap(ConfigurationFieldModel::getFieldValue)
                   .orElse(null);
    }

}
