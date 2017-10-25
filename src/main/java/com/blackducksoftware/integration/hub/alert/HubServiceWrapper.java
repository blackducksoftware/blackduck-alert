/*
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
package com.blackducksoftware.integration.hub.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.repository.AlertProperties;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class HubServiceWrapper {
    private final Logger logger = LoggerFactory.getLogger(HubServiceWrapper.class);

    private final AlertProperties alertProperties;

    @Autowired
    public HubServiceWrapper(final AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    private Slf4jIntLogger slf4jIntLogger;
    private HubServerConfig hubServerConfig;
    private HubServicesFactory hubServicesFactory;

    public void init() throws AlertException {
        try {
            slf4jIntLogger = new Slf4jIntLogger(logger);
            hubServerConfig = createHubServerConfig(slf4jIntLogger);
            hubServicesFactory = createHubServicesFactory(slf4jIntLogger, hubServerConfig);
        } catch (IllegalStateException | EncryptionException e) {
            throw new AlertException("Not able to initialize Hub connection: ", e);
        }
    }

    private HubServicesFactory createHubServicesFactory(final Slf4jIntLogger slf4jIntLogger, final HubServerConfig hubServerConfig) throws EncryptionException {
        final RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(slf4jIntLogger);
        return new HubServicesFactory(restConnection);
    }

    private HubServerConfig createHubServerConfig(final IntLogger slf4jIntLogger) {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(alertProperties.getHubUrl());
        hubServerConfigBuilder.setTimeout(alertProperties.getHubTimeout());
        hubServerConfigBuilder.setUsername(alertProperties.getHubUsername());
        hubServerConfigBuilder.setPassword(alertProperties.getHubPassword());

        hubServerConfigBuilder.setProxyHost(alertProperties.getHubProxyHost());
        hubServerConfigBuilder.setProxyPort(alertProperties.getHubProxyPort());
        hubServerConfigBuilder.setProxyUsername(alertProperties.getHubProxyUsername());
        hubServerConfigBuilder.setProxyPassword(alertProperties.getHubProxyPassword());

        hubServerConfigBuilder.setAlwaysTrustServerCertificate(alertProperties.getHubAlwaysTrustCertificate());
        hubServerConfigBuilder.setLogger(slf4jIntLogger);

        return hubServerConfigBuilder.build();
    }

    public HubServicesFactory getHubServicesFactory() {
        return hubServicesFactory;
    }
}
