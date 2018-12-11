/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.workflow.startup.install;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.FieldConfigurationAccessor;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckProviderUIConfig;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

@Component
public class SystemInitializer {
    private final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);
    private final SystemStatusUtility systemStatusUtility;
    private final AlertProperties alertProperties;
    private final EncryptionUtility encryptionUtility;
    private final SystemValidator systemValidator;
    private final UserAccessor userAccessor;
    private final FieldConfigurationAccessor configurationAccessor;

    @Autowired
    public SystemInitializer(final SystemStatusUtility systemStatusUtility, final AlertProperties alertProperties, final EncryptionUtility encryptionUtility,
        final SystemValidator systemValidator, final UserAccessor userAccessor, final FieldConfigurationAccessor configurationAccessor) {
        this.systemStatusUtility = systemStatusUtility;
        this.alertProperties = alertProperties;
        this.encryptionUtility = encryptionUtility;
        this.systemValidator = systemValidator;
        this.userAccessor = userAccessor;
        this.configurationAccessor = configurationAccessor;
    }

    public boolean isSystemInitialized() {
        return systemStatusUtility.isSystemInitialized();
    }

    @Transactional
    public RequiredSystemConfiguration getCurrentSystemSetup() {
        final Optional<String> proxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> proxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> proxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> proxyPassword = alertProperties.getAlertProxyPassword();
        final Optional<ConfigurationModel> blackDuckConfigEntity = getGlobalBlackDuckConfigEntity();
        String blackDuckUrl = null;
        Integer blackDuckConnectionTimeout = null;
        String blackDuckApiToken = null;
        if (blackDuckConfigEntity.isPresent()) {
            final ConfigurationModel blackDuckEntity = blackDuckConfigEntity.get();
            final FieldAccessor fieldAccessor = new FieldAccessor(blackDuckEntity.getCopyOfKeyToFieldMap());
            blackDuckUrl = fieldAccessor.getString(BlackDuckProviderUIConfig.KEY_BLACKDUCK_URL);
            blackDuckConnectionTimeout = fieldAccessor.getInteger(BlackDuckProviderUIConfig.KEY_BLACKDUCK_TIMEOUT);
            blackDuckApiToken = fieldAccessor.getString(BlackDuckProviderUIConfig.KEY_BLACKDUCK_API_KEY);
        }

        return new RequiredSystemConfiguration(
            true,
            blackDuckUrl,
            blackDuckConnectionTimeout,
            blackDuckApiToken,
            encryptionUtility.isPasswordSet(),
            encryptionUtility.isGlobalSaltSet(),
            proxyHost.orElse(null),
            proxyPort.orElse(null),
            proxyUsername.orElse(null),
            proxyPassword.orElse(null));

    }

    @Transactional
    public boolean updateRequiredConfiguration(final RequiredSystemConfiguration requiredSystemConfiguration, final Map<String, String> fieldErrors) {
        logger.info("updating required configuration for initialization");
        saveEncryptionProperties(requiredSystemConfiguration);
        saveProxySettings(requiredSystemConfiguration);
        final String apiToken = requiredSystemConfiguration.getBlackDuckApiToken();
        final Integer timeout = requiredSystemConfiguration.getBlackDuckConnectionTimeout();
        final String url = requiredSystemConfiguration.getBlackDuckProviderUrl();
        saveBlackDuckConfiguration(timeout, apiToken, url);
        if (StringUtils.isNotBlank(requiredSystemConfiguration.getDefaultAdminPassword())) {
            userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, requiredSystemConfiguration.getDefaultAdminPassword());
        }
        return systemValidator.validate(fieldErrors);
    }

    private void saveProxySettings(final RequiredSystemConfiguration requiredSystemConfiguration) {
        alertProperties.setAlertProxyHost(requiredSystemConfiguration.getProxyHost());
        alertProperties.setAlertProxyPort(requiredSystemConfiguration.getProxyPort());
        alertProperties.setAlertProxyUsername(requiredSystemConfiguration.getProxyUsername());
        alertProperties.setAlertProxyPassword(requiredSystemConfiguration.getProxyPassword());
    }

    private Optional<ConfigurationModel> getGlobalBlackDuckConfigEntity() {
        try {
            final List<ConfigurationModel> globalConfigList = configurationAccessor.getConfigurationByDescriptorNameAndContext(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL);
            if (null == globalConfigList || globalConfigList.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(globalConfigList.get(0));
            }
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error retrieving from DB");
        }
        return Optional.empty();
    }

    private void saveEncryptionProperties(final RequiredSystemConfiguration requiredSystemConfiguration) {
        try {
            encryptionUtility.updateEncryptionFields(requiredSystemConfiguration.getGlobalEncryptionPassword(), requiredSystemConfiguration.getGlobalEncryptionSalt());
        } catch (final IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration during intialization.", ex);
        }
    }

    private void saveBlackDuckConfiguration(final Integer timeout, final String apiToken, final String url) {
        final Optional<ConfigurationModel> blackDuckConfigEntity = getGlobalBlackDuckConfigEntity();

        final ConfigurationFieldModel timeoutField = createField(BlackDuckProviderUIConfig.KEY_BLACKDUCK_TIMEOUT, String.valueOf(timeout));
        final ConfigurationFieldModel apiField = createField(BlackDuckProviderUIConfig.KEY_BLACKDUCK_API_KEY, apiToken);
        final ConfigurationFieldModel urlField = createField(BlackDuckProviderUIConfig.KEY_BLACKDUCK_URL, url);
        try {
            if (blackDuckConfigEntity.isPresent()) {
                final ConfigurationModel configurationModel = blackDuckConfigEntity.get();
                configurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), Arrays.asList(timeoutField, apiField, urlField));
            } else {
                configurationAccessor.createConfiguration(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL, Arrays.asList(timeoutField, apiField, urlField));
            }
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error retrieving from DB");
        }
    }

    private ConfigurationFieldModel createField(final String key, final String value) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValue(value);
        return field;
    }
}
