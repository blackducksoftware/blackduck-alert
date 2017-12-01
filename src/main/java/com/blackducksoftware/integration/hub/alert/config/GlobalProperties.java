/**
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class GlobalProperties {
    private final GlobalHubRepository globalRepository;

    @Value("${blackduck.hub.url:}")
    public String hubUrl;

    @Value("${blackduck.hub.trust.cert:}")
    public Boolean hubTrustCertificate;

    @Value("${blackduck.hub.proxy.host:}")
    public String hubProxyHost;

    @Value("${blackduck.hub.proxy.port:}")
    public String hubProxyPort;

    @Value("${blackduck.hub.proxy.username:}")
    public String hubProxyUsername;

    @Value("${blackduck.hub.proxy.password:}")
    public String hubProxyPassword;

    @Autowired
    public GlobalProperties(final GlobalHubRepository globalRepository) {
        this.globalRepository = globalRepository;
    }

    public GlobalHubConfigEntity getConfig(final Long id) {
        GlobalHubConfigEntity globalConfig = null;
        if (id != null && globalRepository.exists(id)) {
            globalConfig = globalRepository.findOne(id);
        } else {
            globalConfig = getConfig();
        }
        return globalConfig;
    }

    public GlobalHubConfigEntity getConfig() {
        final List<GlobalHubConfigEntity> configs = globalRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return configs.get(0);
        }
        return null;
    }

    public HubServicesFactory createHubServicesFactory(final Logger logger) throws EncryptionException {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        return createHubServicesFactory(intLogger);
    }

    public HubServicesFactory createHubServicesFactory(final IntLogger intLogger) throws EncryptionException {
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

    public HubServerConfig createHubServerConfig(final IntLogger logger) {
        final GlobalHubConfigEntity globalConfigEntity = getConfig();
        if (globalConfigEntity != null) {
            final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
            hubServerConfigBuilder.setHubUrl(hubUrl);
            hubServerConfigBuilder.setTimeout(globalConfigEntity.getHubTimeout());
            hubServerConfigBuilder.setUsername(globalConfigEntity.getHubUsername());
            hubServerConfigBuilder.setPassword(globalConfigEntity.getHubPassword());

            hubServerConfigBuilder.setProxyHost(hubProxyHost);
            hubServerConfigBuilder.setProxyPort(hubProxyPort);
            hubServerConfigBuilder.setProxyUsername(hubProxyUsername);
            hubServerConfigBuilder.setProxyPassword(hubProxyPassword);

            if (hubTrustCertificate != null) {
                hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubTrustCertificate);
            }
            hubServerConfigBuilder.setLogger(logger);

            return hubServerConfigBuilder.build();
        }
        return null;
    }

    public Integer getHubTimeout() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getHubTimeout();
        }
        return null;
    }

    public String getHubUsername() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getHubUsername();
        }
        return null;
    }

    public String getHubPassword() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getHubPassword();
        }
        return null;
    }

    public String getAccumulatorCron() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getAccumulatorCron();
        }
        return null;
    }

    public String getDailyDigestCron() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getDailyDigestCron();
        }
        return null;
    }

    public String getPurgeDataCron() {
        final GlobalHubConfigEntity globalConfig = getConfig();
        if (globalConfig != null) {
            return getConfig().getPurgeDataCron();
        }
        return null;
    }
}
