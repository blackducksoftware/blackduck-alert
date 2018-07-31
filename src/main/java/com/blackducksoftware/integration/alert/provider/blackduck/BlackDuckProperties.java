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
package com.blackducksoftware.integration.alert.provider.blackduck;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.enumeration.AlertEnvironment;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnectionBuilder;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class BlackDuckProperties {
    private final GlobalBlackDuckRepository globalBlackDuckRepository;

    @Value("${blackduck.hub.url:}")
    private String blackDuckUrl;

    @Value("${blackduck.hub.trust.cert:}")
    private Boolean blackDuckTrustCertificate;

    @Value("${blackduck.hub.proxy.host:}")
    private String blackDuckProxyHost;

    @Value("${blackduck.hub.proxy.port:}")
    private String blackDuckProxyPort;

    @Value("${blackduck.hub.proxy.username:}")
    private String blackDuckProxyUsername;

    @Value("${blackduck.hub.proxy.password:}")
    private String blackDuckProxyPassword;

    @Autowired
    public BlackDuckProperties(final GlobalBlackDuckRepository globalBlackDuckRepository) {
        this.globalBlackDuckRepository = globalBlackDuckRepository;
    }

    public String getEnvironmentVariable(final AlertEnvironment alertEnvironment) {
        return getEnvironmentVariable(alertEnvironment.getVariableName());
    }

    public String getEnvironmentVariable(final String variableName) {
        return System.getenv(variableName);
    }

    public Optional<String> getBlackDuckUrl() {
        return Optional.ofNullable(StringUtils.trimToNull(blackDuckUrl));
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
    }

    public Optional<Boolean> getBlackDuckTrustCertificate() {
        // TODO in 3.0.0 we should consider changing the @Value annotations with the new branding names AND @Value will check the environment variables for us, so we wont need to do these extra checks
        if (blackDuckTrustCertificate == null) {
            return Optional.empty();
        }
        final String alwaysTrust = getEnvironmentVariable(AlertEnvironment.HUB_ALWAYS_TRUST_SERVER_CERTIFICATE);
        if (blackDuckTrustCertificate) {
            return Optional.ofNullable(blackDuckTrustCertificate);
        }
        if (StringUtils.isNotBlank(alwaysTrust)) {
            return Optional.ofNullable(Boolean.parseBoolean(alwaysTrust));
        }
        return Optional.of(false);
    }

    public void setBlackDuckTrustCertificate(final Boolean blackDuckTrustCertificate) {
        this.blackDuckTrustCertificate = blackDuckTrustCertificate;
    }

    public Optional<String> getBlackDuckProxyHost() {
        final String proxyHost = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_HOST);
        if (StringUtils.isNotBlank(blackDuckProxyHost)) {
            return Optional.ofNullable(blackDuckProxyHost);
        } else {
            return Optional.ofNullable(proxyHost);
        }
    }

    public void setBlackDuckProxyHost(final String blackDuckProxyHost) {
        this.blackDuckProxyHost = blackDuckProxyHost;
    }

    public Optional<String> getBlackDuckProxyPort() {
        final String proxyPort = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_PORT);
        if (StringUtils.isNotBlank(blackDuckProxyPort)) {
            return Optional.ofNullable(blackDuckProxyPort);
        } else {
            return Optional.ofNullable(proxyPort);
        }
    }

    public void setBlackDuckProxyPort(final String blackDuckProxyPort) {
        this.blackDuckProxyPort = blackDuckProxyPort;
    }

    public Optional<String> getBlackDuckProxyUsername() {
        final String proxyUser = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_USER);
        if (StringUtils.isNotBlank(blackDuckProxyUsername)) {
            return Optional.ofNullable(blackDuckProxyUsername);
        } else {
            return Optional.ofNullable(proxyUser);
        }
    }

    public void setBlackDuckProxyUsername(final String blackDuckProxyUsername) {
        this.blackDuckProxyUsername = blackDuckProxyUsername;
    }

    public Optional<String> getBlackDuckProxyPassword() {
        final String proxyPassword = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_PASSWORD);
        if (StringUtils.isNotBlank(blackDuckProxyPassword)) {
            return Optional.ofNullable(blackDuckProxyPassword);
        } else {
            return Optional.ofNullable(proxyPassword);
        }
    }

    public void setBlackDuckProxyPassword(final String blackDuckProxyPassword) {
        this.blackDuckProxyPassword = blackDuckProxyPassword;
    }

    public Optional<GlobalBlackDuckConfigEntity> getBlackDuckConfig() {
        final List<GlobalBlackDuckConfigEntity> configs = globalBlackDuckRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return Optional.of(configs.get(0));
        }
        return Optional.empty();
    }

    public HubServicesFactory createBlackDuckServicesFactory(final RestConnection restConnection) {
        return new HubServicesFactory(restConnection);
    }

    public Optional<RestConnection> createRestConnectionAndLogErrors(final Logger logger) {
        try {
            return createRestConnection(logger);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<RestConnection> createRestConnection(final Logger logger) throws AlertException {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        return createRestConnection(intLogger);
    }

    public Optional<RestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        final Optional<HubServerConfig> blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
        if (blackDuckServerConfig.isPresent()) {
            return createRestConnection(intLogger, blackDuckServerConfig.get());
        }
        return Optional.empty();
    }

    public Optional<RestConnection> createRestConnection(final IntLogger intLogger, final HubServerConfig blackDuckServerConfig) {
        try {
            return Optional.of(blackDuckServerConfig.createRestConnection(intLogger));
        } catch (final EncryptionException e) {
            intLogger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<HubServerConfig> createBlackDuckServerConfig(final IntLogger logger) throws AlertException {
        final Optional<GlobalBlackDuckConfigEntity> optionalGlobalBlackDuckConfigEntity = getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            final GlobalBlackDuckConfigEntity globalHubConfigEntity = optionalGlobalBlackDuckConfigEntity.get();
            if (globalHubConfigEntity.getBlackDuckTimeout() == null || globalHubConfigEntity.getBlackDuckApiKey() == null) {
                throw new AlertException("Global config settings can not be null.");
            }
            return Optional.of(createBlackDuckServerConfig(logger, globalHubConfigEntity.getBlackDuckTimeout(), globalHubConfigEntity.getBlackDuckApiKey()));
        }
        return Optional.empty();
    }

    public HubServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckApiToken) throws AlertException {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = createBlackDuckServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setApiToken(blackDuckApiToken);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword) throws AlertException {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = createBlackDuckServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfigBuilder createBlackDuckServerConfigBuilderWithoutAuthentication(final IntLogger logger, final int blackDuckTimeout) {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = new HubServerConfigBuilder();
        blackDuckServerConfigBuilder.setLogger(logger);
        blackDuckServerConfigBuilder.setTimeout(blackDuckTimeout);
        final Optional<String> blackDuckUrl = getBlackDuckUrl();
        final Optional<String> blackDuckProxyHost = getBlackDuckProxyHost();
        final Optional<String> blackDuckProxyPort = getBlackDuckProxyPort();
        final Optional<String> blackDuckProxyUsername = getBlackDuckProxyUsername();
        final Optional<String> blackDuckProxyPassword = getBlackDuckProxyPassword();
        final Optional<Boolean> trustCertificate = getBlackDuckTrustCertificate();
        if (blackDuckUrl.isPresent()) {
            blackDuckServerConfigBuilder.setUrl(blackDuckUrl.get());
        }
        if (blackDuckProxyHost.isPresent()) {
            blackDuckServerConfigBuilder.setProxyHost(blackDuckProxyHost.get());
        }
        if (blackDuckProxyPort.isPresent()) {
            blackDuckServerConfigBuilder.setProxyPort(blackDuckProxyPort.get());
        }
        if (blackDuckProxyUsername.isPresent()) {
            blackDuckServerConfigBuilder.setProxyUsername(blackDuckProxyUsername.get());
        }
        if (blackDuckProxyPassword.isPresent()) {
            blackDuckServerConfigBuilder.setProxyPassword(blackDuckProxyPassword.get());
        }
        if (trustCertificate.isPresent()) {
            blackDuckServerConfigBuilder.setTrustCert(trustCertificate.get());
        }

        return blackDuckServerConfigBuilder;
    }

    public UnauthenticatedRestConnectionBuilder createUnauthenticatedRestConnectionBuilder(final IntLogger logger, final int blackDuckTimeout) {
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = new UnauthenticatedRestConnectionBuilder();
        restConnectionBuilder.setLogger(logger);
        restConnectionBuilder.setTimeout(blackDuckTimeout);

        final Optional<String> blackDuckUrl = getBlackDuckUrl();
        final Optional<String> blackDuckProxyHost = getBlackDuckProxyHost();
        final Optional<String> blackDuckProxyPort = getBlackDuckProxyPort();
        final Optional<String> blackDuckProxyUsername = getBlackDuckProxyUsername();
        final Optional<String> blackDuckProxyPassword = getBlackDuckProxyPassword();
        final Optional<Boolean> trustCertificate = getBlackDuckTrustCertificate();
        if (blackDuckUrl.isPresent()) {
            restConnectionBuilder.setBaseUrl(blackDuckUrl.get());
        }
        if (blackDuckProxyHost.isPresent()) {
            restConnectionBuilder.setProxyHost(blackDuckProxyHost.get());
        }
        if (blackDuckProxyPort.isPresent()) {
            restConnectionBuilder.setProxyPort(NumberUtils.toInt(blackDuckProxyPort.get()));
        }
        if (blackDuckProxyUsername.isPresent()) {
            restConnectionBuilder.setProxyUsername(blackDuckProxyUsername.get());
        }
        if (blackDuckProxyPassword.isPresent()) {
            restConnectionBuilder.setProxyPassword(blackDuckProxyPassword.get());
        }
        if (trustCertificate.isPresent()) {
            restConnectionBuilder.setAlwaysTrustServerCertificate(trustCertificate.get());
        }

        return restConnectionBuilder;
    }

    public ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        final Optional<String> blackDuckProxyHost = getBlackDuckProxyHost();
        final Optional<String> blackDuckProxyPort = getBlackDuckProxyPort();
        final Optional<String> blackDuckProxyUsername = getBlackDuckProxyUsername();
        final Optional<String> blackDuckProxyPassword = getBlackDuckProxyPassword();
        if (blackDuckProxyHost.isPresent()) {
            proxyBuilder.setHost(blackDuckProxyHost.get());
        }
        if (blackDuckProxyPort.isPresent()) {
            proxyBuilder.setPort(blackDuckProxyPort.get());
        }
        if (blackDuckProxyUsername.isPresent()) {
            proxyBuilder.setUsername(blackDuckProxyUsername.get());
        }
        if (blackDuckProxyPassword.isPresent()) {
            proxyBuilder.setPassword(blackDuckProxyPassword.get());
        }
        return proxyBuilder;
    }

    public Integer getBlackDuckTimeout() {
        final Optional<GlobalBlackDuckConfigEntity> optionalGlobalBlackDuckConfigEntity = getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            return optionalGlobalBlackDuckConfigEntity.get().getBlackDuckTimeout();
        }
        return 300;
    }

}
