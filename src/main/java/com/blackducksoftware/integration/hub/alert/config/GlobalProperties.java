/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class GlobalProperties {
    private final GlobalHubRepositoryWrapper globalHubRepository;
    private final GlobalSchedulingRepositoryWrapper globalSchedulingRepository;

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

    @Autowired
    public GlobalProperties(final GlobalHubRepositoryWrapper globalRepository, final GlobalSchedulingRepositoryWrapper globalSchedulingRepository) {
        this.globalHubRepository = globalRepository;
        this.globalSchedulingRepository = globalSchedulingRepository;
    }

    public String getHubUrl() {
        try {
            final URL extensionUrl = new URL(hubUrl);
            if (extensionUrl.getHost().equals("localhost")) {
                final String hostName = Optional.ofNullable(System.getenv("PUBLIC_HUB_WEBSERVER_HOST")).orElseGet(() -> HostnameHelper.getMyHostname());
                final URL url = new URL(extensionUrl.getProtocol(), hostName, extensionUrl.getPort(), extensionUrl.getFile());
                return url.toString();
            }
        } catch (final MalformedURLException e) {
            return hubUrl;
        }
        return hubUrl;
    }

    public Boolean getHubTrustCertificate() {
        final String alwaysTrust = System.getenv("HUB_ALWAYS_TRUST_SERVER_CERTIFICATE");
        if (StringUtils.isNotBlank(alwaysTrust)) {
            return Boolean.parseBoolean(alwaysTrust);
        }
        return hubTrustCertificate;
    }

    public String getHubProxyHost() {
        final String proxyHost = System.getenv("HUB_PROXY_HOST");
        if (StringUtils.isEmpty(proxyHost)) {
            return hubProxyHost;
        } else {
            return proxyHost;
        }
    }

    public String getHubProxyPort() {
        final String proxyPort = System.getenv("HUB_PROXY_PORT");
        if (StringUtils.isEmpty(proxyPort)) {
            return hubProxyPort;
        } else {
            return proxyPort;
        }
    }

    public String getHubProxyUsername() {
        final String proxyUser = System.getenv("HUB_PROXY_USER");
        if (StringUtils.isEmpty(proxyUser)) {
            return hubProxyUsername;
        } else {
            return proxyUser;
        }
    }

    public String getHubProxyPassword() {
        final String proxyPassword = System.getenv("HUB_PROXY_PASSWORD");
        if (StringUtils.isEmpty(proxyPassword)) {
            return hubProxyPassword;
        } else {
            return proxyPassword;
        }
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public void setHubTrustCertificate(final Boolean hubTrustCertificate) {
        this.hubTrustCertificate = hubTrustCertificate;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public GlobalHubConfigEntity getConfig(final Long id) {
        GlobalHubConfigEntity globalConfig = null;
        if (id != null && globalHubRepository.exists(id)) {
            globalConfig = globalHubRepository.findOne(id);
        } else {
            globalConfig = getHubConfig();
        }
        return globalConfig;
    }

    public GlobalHubConfigEntity getHubConfig() {
        final List<GlobalHubConfigEntity> configs = globalHubRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return configs.get(0);
        }
        return null;
    }

    public GlobalSchedulingConfigEntity getSchedulingConfig() {
        final List<GlobalSchedulingConfigEntity> configs = globalSchedulingRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return configs.get(0);
        }
        return null;
    }

    public HubServicesFactory createHubServicesFactory(final Logger logger) throws IntegrationException {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        return createHubServicesFactory(intLogger);
    }

    public HubServicesFactory createHubServicesFactory(final IntLogger intLogger) throws IntegrationException {
        final HubServerConfig hubServerConfig = createHubServerConfig(intLogger);
        if (hubServerConfig != null) {
            final RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(intLogger);
            return new HubServicesFactory(restConnection);
        }
        return null;
    }

    public HubServicesFactory createHubServicesFactoryAndLogErrors(final Logger logger) {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        try {
            return createHubServicesFactory(intLogger);
        } catch (final Exception e) {
            intLogger.error(e.getMessage(), e);
        }
        return null;
    }

    public HubServerConfig createHubServerConfig(final IntLogger logger) throws AlertException {
        final GlobalHubConfigEntity globalConfigEntity = getHubConfig();
        if (globalConfigEntity != null) {
            final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
            hubServerConfigBuilder.setHubUrl(getHubUrl());
            hubServerConfigBuilder.setTimeout(globalConfigEntity.getHubTimeout());
            hubServerConfigBuilder.setUsername(globalConfigEntity.getHubUsername());
            hubServerConfigBuilder.setPassword(globalConfigEntity.getHubPassword());

            hubServerConfigBuilder.setProxyHost(getHubProxyHost());
            hubServerConfigBuilder.setProxyPort(getHubProxyPort());
            hubServerConfigBuilder.setProxyUsername(getHubProxyUsername());
            hubServerConfigBuilder.setProxyPassword(getHubProxyPassword());

            if (hubTrustCertificate != null) {
                hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubTrustCertificate);
            }
            hubServerConfigBuilder.setLogger(logger);

            try {
                return hubServerConfigBuilder.build();
            } catch (final IllegalStateException e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    public Integer getHubTimeout() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getHubConfig().getHubTimeout();
        }
        return null;
    }

    public String getHubUsername() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getHubConfig().getHubUsername();
        }
        return null;
    }

    public String getHubPassword() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getHubConfig().getHubPassword();
        }
        return null;
    }

    public String getAccumulatorCron() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getSchedulingConfig().getAccumulatorCron();
        }
        return null;
    }

    public String getDailyDigestCron() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getSchedulingConfig().getDailyDigestCron();
        }
        return null;
    }

    public String getPurgeDataCron() {
        final GlobalHubConfigEntity globalConfig = getHubConfig();
        if (globalConfig != null) {
            return getSchedulingConfig().getPurgeDataCron();
        }
        return null;
    }
}
