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
package com.blackducksoftware.integration.alert.common;

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnectionBuilder;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class AlertProperties {
    @Value("${alert.config.home:}")
    private String alertConfigHome;

    @Value("${alert.templates.dir:}")
    private String alertTemplatesDir;

    @Value("${alert.images.dir:}")
    private String alertImagesDir;

    @Value("${alert.trust.cert:}")
    private Boolean alertTrustCertificate;

    @Value("${alert.proxy.host:}")
    private String alertProxyHost;

    @Value("${alert.proxy.port:}")
    private String alertProxyPort;

    @Value("${alert.proxy.username:}")
    private String alertProxyUsername;

    @Value("${alert.proxy.password:}")
    private String alertProxyPassword;

    public String getAlertConfigHome() {
        return StringUtils.trimToNull(alertConfigHome);
    }

    public String getAlertTemplatesDir() {
        return StringUtils.trimToNull(alertTemplatesDir);
    }

    public String getAlertImagesDir() {
        return StringUtils.trimToNull(alertImagesDir);
    }

    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public Optional<String> getAlertProxyHost() {
        return getOptionalString(alertProxyHost);
    }

    public Optional<String> getAlertProxyPort() {
        return getOptionalString(alertProxyPort);
    }

    public Optional<String> getAlertProxyUsername() {
        return getOptionalString(alertProxyUsername);
    }

    public Optional<String> getAlertProxyPassword() {
        return getOptionalString(alertProxyPassword);
    }

    private Optional<String> getOptionalString(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public UnauthenticatedRestConnectionBuilder createUnauthenticatedRestConnectionBuilder(final IntLogger logger, final String baseUrl, final int blackDuckTimeout) {
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = new UnauthenticatedRestConnectionBuilder();
        restConnectionBuilder.setLogger(logger);
        restConnectionBuilder.setTimeout(blackDuckTimeout);

        final Optional<String> alertProxyHost = getAlertProxyHost();
        final Optional<String> alertProxyPort = getAlertProxyPort();
        final Optional<String> alertProxyUsername = getAlertProxyUsername();
        final Optional<String> alertProxyPassword = getAlertProxyPassword();
        final Optional<Boolean> alertTrustCertificate = getAlertTrustCertificate();
        restConnectionBuilder.setBaseUrl(baseUrl);
        if (alertProxyHost.isPresent()) {
            restConnectionBuilder.setProxyHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            restConnectionBuilder.setProxyPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            restConnectionBuilder.setProxyUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            restConnectionBuilder.setProxyPassword(alertProxyPassword.get());
        }
        if (alertTrustCertificate.isPresent()) {
            restConnectionBuilder.setAlwaysTrustServerCertificate(alertTrustCertificate.get());
        }

        return restConnectionBuilder;
    }

    public ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        final Optional<String> alertProxyHost = getAlertProxyHost();
        final Optional<String> alertProxyPort = getAlertProxyPort();
        final Optional<String> alertProxyUsername = getAlertProxyUsername();
        final Optional<String> alertProxyPassword = getAlertProxyPassword();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            proxyBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            proxyBuilder.setPassword(alertProxyPassword.get());
        }
        return proxyBuilder;
    }

    public Properties getBlackDuckProperties() {
        final Properties properties = new Properties();
        properties.setProperty(HubServerConfigBuilder.HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "trust.cert", String.valueOf(getAlertTrustCertificate().orElse(false)));
        properties.setProperty(HubServerConfigBuilder.HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.host", getAlertProxyHost().orElse(""));
        properties.setProperty(HubServerConfigBuilder.HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.port", getAlertProxyPort().orElse(""));
        properties.setProperty(HubServerConfigBuilder.HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.username", getAlertProxyUsername().orElse(""));
        properties.setProperty(HubServerConfigBuilder.HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.password", getAlertProxyPassword().orElse(""));
        return properties;
    }
}
