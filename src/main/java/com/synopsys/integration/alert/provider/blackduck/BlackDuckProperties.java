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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.hub.configuration.HubServerConfig;
import com.synopsys.integration.hub.configuration.HubServerConfigBuilder;
import com.synopsys.integration.hub.rest.BlackduckRestConnection;
import com.synopsys.integration.hub.service.HubServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckProperties {
    private final GlobalBlackDuckRepository globalBlackDuckRepository;
    private final AlertProperties alertProperties;

    @Value("${blackduck.url:}")
    private String blackDuckUrl;

    @Value("${public.blackduck.webserver.host:}")
    private String publicBlackDuckWebserverHost;

    @Value("${public.blackduck.webserver.port:}")
    private String publicBlackDuckWebserverPort;

    @Autowired
    public BlackDuckProperties(final GlobalBlackDuckRepository globalBlackDuckRepository, final AlertProperties alertProperties) {
        this.globalBlackDuckRepository = globalBlackDuckRepository;
        this.alertProperties = alertProperties;
    }

    public Optional<String> getBlackDuckUrl() {
        return getOptionalString(blackDuckUrl);
    }

    public Optional<String> getPublicBlackDuckWebserverHost() {
        return getOptionalString(publicBlackDuckWebserverHost);
    }

    public Optional<String> getPublicBlackDuckWebserverPort() {
        return getOptionalString(publicBlackDuckWebserverPort);
    }

    private Optional<String> getOptionalString(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public Integer getBlackDuckTimeout() {
        final Optional<GlobalBlackDuckConfigEntity> optionalGlobalBlackDuckConfigEntity = getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfigEntity.isPresent()) {
            return optionalGlobalBlackDuckConfigEntity.get().getBlackDuckTimeout();
        }
        return 300;
    }

    public Optional<GlobalBlackDuckConfigEntity> getBlackDuckConfig() {
        final List<GlobalBlackDuckConfigEntity> configs = globalBlackDuckRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            return Optional.of(configs.get(0));
        }
        return Optional.empty();
    }

    public HubServicesFactory createBlackDuckServicesFactory(final BlackduckRestConnection restConnection, final IntLogger logger) {
        return new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
    }

    public Optional<BlackduckRestConnection> createRestConnectionAndLogErrors(final Logger logger) {
        try {
            return createRestConnection(logger);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<BlackduckRestConnection> createRestConnection(final Logger logger) throws AlertException {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        return createRestConnection(intLogger);
    }

    public Optional<BlackduckRestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        final Optional<HubServerConfig> blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
        if (blackDuckServerConfig.isPresent()) {
            return createRestConnection(intLogger, blackDuckServerConfig.get());
        }
        return Optional.empty();
    }

    public Optional<BlackduckRestConnection> createRestConnection(final IntLogger intLogger, final HubServerConfig blackDuckServerConfig) {
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
        final HubServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setApiToken(blackDuckApiToken);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword) throws AlertException {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public HubServerConfigBuilder createServerConfigBuilderWithoutAuthentication(final IntLogger logger, final int blackDuckTimeout) {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = new HubServerConfigBuilder();
        blackDuckServerConfigBuilder.setFromProperties(alertProperties.getBlackDuckProperties());
        blackDuckServerConfigBuilder.setLogger(logger);
        blackDuckServerConfigBuilder.setTimeout(blackDuckTimeout);
        blackDuckServerConfigBuilder.setUrl(getBlackDuckUrl().orElse(""));

        return blackDuckServerConfigBuilder;
    }

}
