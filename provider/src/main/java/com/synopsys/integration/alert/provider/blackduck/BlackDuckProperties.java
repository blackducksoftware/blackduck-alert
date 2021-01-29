/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BlackDuckProperties extends ProviderProperties {
    public static final int DEFAULT_TIMEOUT = 300;
    private final Gson gson;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final String url;
    private final Integer timeout;
    private final String apiToken;

    public BlackDuckProperties(Long configId, Gson gson, AlertProperties alertProperties, ProxyManager proxyManager, ConfigurationModel configurationModel) {
        this(configId, gson, alertProperties, proxyManager, createFieldAccessor(configurationModel));
    }

    public BlackDuckProperties(Long configId, Gson gson, AlertProperties alertProperties, ProxyManager proxyManager, FieldUtility fieldUtility) {
        super(configId, fieldUtility);
        this.gson = gson;
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.url = fieldUtility
                       .getString(BlackDuckDescriptor.KEY_BLACKDUCK_URL)
                       .filter(StringUtils::isNotBlank)
                       .orElse(null);
        this.timeout = fieldUtility
                           .getInteger(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT)
                           .orElse(DEFAULT_TIMEOUT);
        this.apiToken = fieldUtility.getStringOrNull(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
    }

    private static FieldUtility createFieldAccessor(ConfigurationModel configurationModel) {
        return Optional.ofNullable(configurationModel)
                   .map(config -> new FieldUtility(config.getCopyOfKeyToFieldMap()))
                   .orElse(new FieldUtility(Map.of()));
    }

    public Optional<String> getBlackDuckUrl() {
        return Optional.ofNullable(url);
    }

    public Integer getBlackDuckTimeout() {
        return timeout;
    }

    public String getApiToken() {
        return apiToken;
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(BlackDuckHttpClient blackDuckHttpClient, IntLogger logger) {
        return new BlackDuckServicesFactory(
            IntEnvironmentVariables.empty(), gson, BlackDuckServicesFactory.createDefaultObjectMapper(), new NoThreadExecutorService(), blackDuckHttpClient, logger, BlackDuckServicesFactory.createDefaultRequestFactory());
    }

    public Optional<BlackDuckHttpClient> createBlackDuckHttpClientAndLogErrors(Logger logger) {
        BlackDuckHttpClient blackDuckHttpClient = null;
        try {
            blackDuckHttpClient = createBlackDuckHttpClient(logger);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.ofNullable(blackDuckHttpClient);
    }

    public BlackDuckHttpClient createBlackDuckHttpClient(Logger logger) throws AlertException {
        IntLogger intLogger = new Slf4jIntLogger(logger);
        return createBlackDuckHttpClient(intLogger);
    }

    public BlackDuckHttpClient createBlackDuckHttpClient(IntLogger intLogger) throws AlertException {
        BlackDuckServerConfig blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
        return createBlackDuckHttpClient(intLogger, blackDuckServerConfig);
    }

    public BlackDuckHttpClient createBlackDuckCacheClient(IntLogger intLogger) throws AlertException {
        BlackDuckServerConfig blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
        return blackDuckServerConfig.createCacheHttpClient(intLogger);
    }

    public BlackDuckHttpClient createBlackDuckHttpClient(IntLogger intLogger, BlackDuckServerConfig blackDuckServerConfig) {
        return blackDuckServerConfig.createBlackDuckHttpClient(intLogger);
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(IntLogger logger) throws AlertException {
        if (apiToken == null) {
            throw new AlertException("Invalid global config settings. API Token is null.");
        }
        return createBlackDuckServerConfig(logger, timeout, apiToken);
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(IntLogger logger, int blackDuckTimeout, String blackDuckApiToken) throws AlertException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setApiToken(blackDuckApiToken);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(IntLogger logger, int blackDuckTimeout, String blackDuckUsername, String blackDuckPassword) throws AlertException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public BlackDuckServerConfigBuilder createServerConfigBuilderWithoutAuthentication(IntLogger logger, int blackDuckTimeout) {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setProperties(getBlackDuckProperties().entrySet());
        blackDuckServerConfigBuilder.setLogger(logger);
        blackDuckServerConfigBuilder.setTimeoutInSeconds(blackDuckTimeout);
        blackDuckServerConfigBuilder.setUrl(getBlackDuckUrl().orElse(""));

        return blackDuckServerConfigBuilder;
    }

    private Map<String, String> getBlackDuckProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(BlackDuckServerConfigBuilder.TRUST_CERT_KEY.getKey(), String.valueOf(alertProperties.getAlertTrustCertificate().orElse(false)));

        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        properties.put(BlackDuckServerConfigBuilder.PROXY_HOST_KEY.getKey(), proxyInfo.getHost().orElse(""));
        properties.put(BlackDuckServerConfigBuilder.PROXY_PORT_KEY.getKey(), String.valueOf(proxyInfo.getPort()));
        properties.put(BlackDuckServerConfigBuilder.PROXY_USERNAME_KEY.getKey(), proxyInfo.getUsername().orElse(""));
        properties.put(BlackDuckServerConfigBuilder.PROXY_PASSWORD_KEY.getKey(), proxyInfo.getPassword().orElse(""));
        return properties;
    }

}
