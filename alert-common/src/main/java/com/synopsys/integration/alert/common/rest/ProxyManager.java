/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.rest;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class ProxyManager {
    public static final String KEY_PROXY_HOST = "settings.proxy.host";
    public static final String KEY_PROXY_PORT = "settings.proxy.port";
    public static final String KEY_PROXY_USERNAME = "settings.proxy.username";
    public static final String KEY_PROXY_PWD = "settings.proxy.password";

    private final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    private SettingsUtility settingsUtility;

    @Autowired
    public ProxyManager(SettingsUtility settingsUtility) {
        this.settingsUtility = settingsUtility;
    }

    private Optional<ConfigurationModel> getSettingsConfiguration() {
        try {
            return settingsUtility.getConfiguration();
        } catch (AlertException ex) {
            logger.error("Could not find the settings configuration for proxy data", ex);
        }
        return Optional.empty();
    }

    public ProxyInfo createProxyInfo() throws IllegalArgumentException {
        Optional<ConfigurationModel> settingsConfiguration = getSettingsConfiguration();
        Optional<String> alertProxyHost = getProxySetting(settingsConfiguration, KEY_PROXY_HOST);
        Optional<String> alertProxyPort = getProxySetting(settingsConfiguration, KEY_PROXY_PORT);
        Optional<String> alertProxyUsername = getProxySetting(settingsConfiguration, KEY_PROXY_USERNAME);
        Optional<String> alertProxyPassword = getProxySetting(settingsConfiguration, KEY_PROXY_PWD);

        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        if (alertProxyUsername.isPresent()) {
            credentialsBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            credentialsBuilder.setPassword(alertProxyPassword.get());
        }
        proxyBuilder.setCredentials(credentialsBuilder.build());
        return proxyBuilder.build();
    }

    public Optional<String> getProxyHost() {
        return getProxySetting(KEY_PROXY_HOST);
    }

    public Optional<String> getProxyPort() {
        return getProxySetting(KEY_PROXY_PORT);
    }

    public Optional<String> getProxyUsername() {
        return getProxySetting(KEY_PROXY_USERNAME);
    }

    public Optional<String> getProxyPassword() {
        return getProxySetting(KEY_PROXY_PWD);
    }

    private Optional<String> getProxySetting(String key) {
        Optional<ConfigurationModel> settingsConfiguration = getSettingsConfiguration();
        return getProxySetting(settingsConfiguration, key);
    }

    private Optional<String> getProxySetting(Optional<ConfigurationModel> settingsConfiguration, String key) {
        return settingsConfiguration.flatMap(configurationModel -> configurationModel.getField(key)).flatMap(ConfigurationFieldModel::getFieldValue);
    }

    public Proxy createProxy() {
        if (!getProxyHost().isPresent()) {
            return Proxy.NO_PROXY;
        }
        //InetSocketAddress will validate the host name isn't null and the port is in the correct range.
        String hostname = getProxyHost().orElse(null);
        String port = getProxyPort().orElse("-1");
        InetSocketAddress socketAddress = new InetSocketAddress(hostname, Integer.valueOf(port));

        return new Proxy(Proxy.Type.HTTP, socketAddress);
    }

}
