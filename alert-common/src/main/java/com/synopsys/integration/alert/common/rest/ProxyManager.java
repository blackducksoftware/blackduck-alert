/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest;

import java.net.Proxy;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
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

    private final SettingsUtility settingsUtility;

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
        return createProxyInfo()
                   .getProxy()
                   .orElse(Proxy.NO_PROXY);
    }

}
