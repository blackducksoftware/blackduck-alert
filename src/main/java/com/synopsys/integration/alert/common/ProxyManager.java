/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common;

import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class ProxyManager {
    private final BaseConfigurationAccessor configurationAccessor;

    @Autowired
    public ProxyManager(final BaseConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    private ConfigurationModel getSettingsConfiguration() throws AlertRuntimeException, AlertDatabaseConstraintException {
        return configurationAccessor.getConfigurationByDescriptorNameAndContext(SettingsDescriptor.SETTINGS_COMPONENT, ConfigContextEnum.GLOBAL)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertRuntimeException("Settings configuration missing"));
    }

    public Optional<String> getAlertProxyHost() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_HOST).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyPort() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_PORT).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyUsername() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_USERNAME).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyPassword() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_PASSWORD).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public ProxyInfo createProxyInfo() throws AlertRuntimeException, AlertDatabaseConstraintException, IllegalArgumentException {
        ConfigurationModel settingsConfiguration = getSettingsConfiguration();
        final Optional<String> alertProxyHost = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_HOST).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyPort = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_PORT).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyUsername = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_USERNAME).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyPassword = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_PASSWORD).flatMap(ConfigurationFieldModel::getFieldValue);

        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        final CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        if (alertProxyUsername.isPresent()) {
            credentialsBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            credentialsBuilder.setPassword(alertProxyPassword.get());
        }
        proxyBuilder.setCredentials(credentialsBuilder.build());
        return proxyBuilder.build();
    }
}
