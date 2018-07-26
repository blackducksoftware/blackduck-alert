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
package com.blackducksoftware.integration.alert.config;

import java.nio.charset.StandardCharsets;
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
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.web.model.AboutModel;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnectionBuilder;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.Gson;

@Component
public class GlobalProperties {
    public final static String PRODUCT_VERSION_UNKNOWN = "unknown";
    private final GlobalHubRepository globalHubRepository;

    @Value("${blackduck.hub.url:}")
    private String hubUrl;

    @Value("${blackduck.hub.trust.cert:}")
    private Boolean hubTrustCertificate;

    @Value("${blackduck.hub.proxy.host:}")
    private String hubProxyHost;

    @Value("${blackduck.hub.proxy.port:}")
    private String hubProxyPort;

    @Value("${blackduck.hub.proxy.username:}")
    private String hubProxyUsername;

    @Value("${blackduck.hub.proxy.password:}")
    private String hubProxyPassword;

    // SSL properties

    @Value("${server.port:")
    private String serverPort;

    @Value("${server.ssl.key-store:}")
    private String keyStoreFile;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePass;

    @Value("${server.ssl.keyStoreType:}")
    private String keyStoreType;

    @Value("${server.ssl.keyAlias:}")
    private String keyAlias;

    @Value("${server.ssl.trust-store:}")
    private String trustStoreFile;

    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePass;

    @Value("${server.ssl.trustStoreType:}")
    private String trustStoreType;

    private AboutModel aboutModel;

    @Autowired
    public GlobalProperties(final GlobalHubRepository globalRepository, final Gson gson) {
        this.globalHubRepository = globalRepository;
        readAboutInformation(gson);
    }

    protected void readAboutInformation(final Gson gson) {
        try {
            final String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
            aboutModel = gson.fromJson(aboutJson, AboutModel.class);
        } catch (final Exception e) {
            aboutModel = null;
            throw new RuntimeException(e);
        }
    }

    public String getProductVersion() {
        if (aboutModel != null) {
            return aboutModel.getVersion();
        } else {
            return PRODUCT_VERSION_UNKNOWN;
        }
    }

    public Optional<AboutModel> getAboutModel() {
        return Optional.ofNullable(aboutModel);
    }

    public String getEnvironmentVariable(final AlertEnvironment alertEnvironment) {
        return getEnvironmentVariable(alertEnvironment.getVariableName());
    }

    public String getEnvironmentVariable(final String variableName) {
        return System.getenv(variableName);
    }

    public Optional<String> getHubUrl() {
        return Optional.ofNullable(StringUtils.trimToNull(hubUrl));
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public Optional<Boolean> getHubTrustCertificate() {
        // TODO in 3.0.0 we should consider changing the @Value annotations with the new branding names AND @Value will check the environment variables for us, so we wont need to do these extra checks
        if (hubTrustCertificate == null) {
            return Optional.empty();
        }
        final String alwaysTrust = getEnvironmentVariable(AlertEnvironment.HUB_ALWAYS_TRUST_SERVER_CERTIFICATE);
        if (hubTrustCertificate) {
            return Optional.ofNullable(hubTrustCertificate);
        }
        if (StringUtils.isNotBlank(alwaysTrust)) {
            return Optional.ofNullable(Boolean.parseBoolean(alwaysTrust));
        }
        return Optional.of(false);
    }

    public void setHubTrustCertificate(final Boolean hubTrustCertificate) {
        this.hubTrustCertificate = hubTrustCertificate;
    }

    public Optional<String> getHubProxyHost() {
        final String proxyHost = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_HOST);
        if (StringUtils.isNotBlank(hubProxyHost)) {
            return Optional.ofNullable(hubProxyHost);
        } else {
            return Optional.ofNullable(proxyHost);
        }
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public Optional<String> getHubProxyPort() {
        final String proxyPort = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_PORT);
        if (StringUtils.isNotBlank(hubProxyPort)) {
            return Optional.ofNullable(hubProxyPort);
        } else {
            return Optional.ofNullable(proxyPort);
        }
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public Optional<String> getHubProxyUsername() {
        final String proxyUser = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_USER);
        if (StringUtils.isNotBlank(hubProxyUsername)) {
            return Optional.ofNullable(hubProxyUsername);
        } else {
            return Optional.ofNullable(proxyUser);
        }
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public Optional<String> getHubProxyPassword() {
        final String proxyPassword = getEnvironmentVariable(AlertEnvironment.HUB_PROXY_PASSWORD);
        if (StringUtils.isNotBlank(hubProxyPassword)) {
            return Optional.ofNullable(hubProxyPassword);
        } else {
            return Optional.ofNullable(proxyPassword);
        }
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public Optional<GlobalHubConfigEntity> getHubConfig() {
        final List<GlobalHubConfigEntity> configs = globalHubRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return Optional.of(configs.get(0));
        }
        return Optional.empty();
    }

    public HubServicesFactory createHubServicesFactory(final RestConnection restConnection) {
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
        final Optional<HubServerConfig> hubServerConfig = createHubServerConfig(intLogger);
        if (hubServerConfig.isPresent()) {
            return createRestConnection(intLogger, hubServerConfig.get());
        }
        return Optional.empty();
    }

    public Optional<RestConnection> createRestConnection(final IntLogger intLogger, final HubServerConfig hubServerConfig) {
        try {
            return Optional.of(hubServerConfig.createRestConnection(intLogger));
        } catch (final EncryptionException e) {
            intLogger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<HubServerConfig> createHubServerConfig(final IntLogger logger) throws AlertException {
        final Optional<GlobalHubConfigEntity> optionalGlobalHubConfigEntity = getHubConfig();
        if (optionalGlobalHubConfigEntity.isPresent()) {
            final GlobalHubConfigEntity globalHubConfigEntity = optionalGlobalHubConfigEntity.get();
            if (globalHubConfigEntity.getHubTimeout() == null || globalHubConfigEntity.getHubApiKey() == null) {
                throw new AlertException("Global config settings can not be null.");
            }
            return Optional.of(createHubServerConfig(logger, globalHubConfigEntity.getHubTimeout(), globalHubConfigEntity.getHubApiKey()));
        }
        return Optional.empty();
    }

    public HubServerConfig createHubServerConfig(final IntLogger logger, final int hubTimeout, final String hubApiToken) throws AlertException {
        final HubServerConfigBuilder hubServerConfigBuilder = createHubServerConfigBuilderWithoutAuthentication(logger, hubTimeout);
        hubServerConfigBuilder.setApiToken(hubApiToken);

        try {
            return hubServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfig createHubServerConfig(final IntLogger logger, final int hubTimeout, final String hubUsername, final String hubPassword) throws AlertException {
        final HubServerConfigBuilder hubServerConfigBuilder = createHubServerConfigBuilderWithoutAuthentication(logger, hubTimeout);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);

        try {
            return hubServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfigBuilder createHubServerConfigBuilderWithoutAuthentication(final IntLogger logger, final int hubTimeout) {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setLogger(logger);
        hubServerConfigBuilder.setTimeout(hubTimeout);

        if (getHubUrl().isPresent()) {
            hubServerConfigBuilder.setUrl(getHubUrl().get());
        }
        if (getHubProxyHost().isPresent()) {
            hubServerConfigBuilder.setProxyHost(getHubProxyHost().get());
        }
        if (getHubProxyPort().isPresent()) {
            hubServerConfigBuilder.setProxyPort(getHubProxyPort().get());
        }
        if (getHubProxyUsername().isPresent()) {
            hubServerConfigBuilder.setProxyUsername(getHubProxyUsername().get());
        }
        if (getHubProxyPassword().isPresent()) {
            hubServerConfigBuilder.setProxyPassword(getHubProxyPassword().get());
        }
        if (getHubTrustCertificate().isPresent()) {
            hubServerConfigBuilder.setTrustCert(getHubTrustCertificate().get());
        }

        return hubServerConfigBuilder;
    }

    public UnauthenticatedRestConnectionBuilder createUnauthenticatedRestConnectionBuilder(final IntLogger logger, final int hubTimeout) {
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = new UnauthenticatedRestConnectionBuilder();
        restConnectionBuilder.setLogger(logger);
        restConnectionBuilder.setTimeout(hubTimeout);

        if (getHubUrl().isPresent()) {
            restConnectionBuilder.setBaseUrl(getHubUrl().get());
        }
        if (getHubProxyHost().isPresent()) {
            restConnectionBuilder.setProxyHost(getHubProxyHost().get());
        }
        if (getHubProxyPort().isPresent()) {
            restConnectionBuilder.setProxyPort(NumberUtils.toInt(getHubProxyPort().get()));
        }
        if (getHubProxyUsername().isPresent()) {
            restConnectionBuilder.setProxyUsername(getHubProxyUsername().get());
        }
        if (getHubProxyPassword().isPresent()) {
            restConnectionBuilder.setProxyPassword(getHubProxyPassword().get());
        }
        if (getHubTrustCertificate().isPresent()) {
            restConnectionBuilder.setAlwaysTrustServerCertificate(getHubTrustCertificate().get());
        }

        return restConnectionBuilder;
    }

    public ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        if (getHubProxyHost().isPresent()) {
            proxyBuilder.setHost(getHubProxyHost().get());
        }
        if (getHubProxyPort().isPresent()) {
            proxyBuilder.setPort(getHubProxyPort().get());
        }
        if (getHubProxyUsername().isPresent()) {
            proxyBuilder.setUsername(getHubProxyUsername().get());
        }
        if (getHubProxyPassword().isPresent()) {
            proxyBuilder.setPassword(getHubProxyPassword().get());
        }
        return proxyBuilder;
    }

    public Integer getHubTimeout() {
        final Optional<GlobalHubConfigEntity> globalConfig = getHubConfig();
        if (globalConfig.isPresent()) {
            return getHubConfig().get().getHubTimeout();
        }
        return 300;
    }

    public Optional<String> getHubApiKey() {
        final Optional<GlobalHubConfigEntity> globalConfig = getHubConfig();
        if (globalConfig.isPresent()) {
            return Optional.of(globalConfig.get().getHubApiKey());
        }
        return Optional.empty();
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(final String serverPort) {
        this.serverPort = serverPort;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(final String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(final String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(final String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(final String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(final String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public void setTrustStorePass(final String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(final String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}
