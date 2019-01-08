/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckProperties extends ProviderProperties {
    public static final int DEFAULT_TIMEOUT = 300;

    private final Gson gson;
    private final AlertProperties alertProperties;
    private final Logger classLogger = LoggerFactory.getLogger(getClass());

    // the blackduck product hasn't renamed their environment variables from hub to blackduck
    // need to keep hub in the name until
    @Value("${public.hub.webserver.host:}")
    private String publicBlackDuckWebserverHost;

    @Value("${public.hub.webserver.port:}")
    private String publicBlackDuckWebserverPort;

    @Autowired
    public BlackDuckProperties(final Gson gson, final AlertProperties alertProperties, final BaseConfigurationAccessor configurationAccessor) {
        super(BlackDuckProvider.COMPONENT_NAME, configurationAccessor);
        this.gson = gson;
        this.alertProperties = alertProperties;
    }

    public Optional<String> getBlackDuckUrl() {
        return createFieldAccessor()
                   .getString(BlackDuckDescriptor.KEY_BLACKDUCK_URL)
                   .filter(StringUtils::isNotBlank);
    }

    public Integer getBlackDuckTimeout() {
        return createFieldAccessor()
                   .getInteger(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT)
                   .orElse(DEFAULT_TIMEOUT);
    }

    public Optional<String> getPublicBlackDuckWebserverHost() {
        return createOptionalString(publicBlackDuckWebserverHost);
    }

    public Optional<String> getPublicBlackDuckWebserverPort() {
        return createOptionalString(publicBlackDuckWebserverPort);
    }

    public Optional<ConfigurationModel> getBlackDuckConfig() {
        return retrieveGlobalConfig();
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final BlackDuckRestConnection restConnection, final IntLogger logger) {
        return new BlackDuckServicesFactory(gson, BlackDuckServicesFactory.createDefaultObjectMapper(), restConnection, logger);
    }

    public Optional<BlackDuckRestConnection> createRestConnectionAndLogErrors(final Logger logger) {
        try {
            return createRestConnection(logger);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<BlackDuckRestConnection> createRestConnection(final Logger logger) throws AlertException {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        return createRestConnection(intLogger);
    }

    public Optional<BlackDuckRestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        final Optional<BlackDuckServerConfig> blackDuckServerConfig = createBlackDuckServerConfig(intLogger);
        if (blackDuckServerConfig.isPresent()) {
            return createRestConnection(intLogger, blackDuckServerConfig.get());
        }
        return Optional.empty();
    }

    public Optional<BlackDuckRestConnection> createRestConnection(final IntLogger intLogger, final BlackDuckServerConfig blackDuckServerConfig) {
        try {
            return Optional.of(blackDuckServerConfig.createRestConnection(intLogger));
        } catch (final Exception e) {
            intLogger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<BlackDuckServerConfig> createBlackDuckServerConfig(final IntLogger logger) throws AlertException {
        final Optional<ConfigurationModel> optionalGlobalBlackDuckConfig = getBlackDuckConfig();
        if (optionalGlobalBlackDuckConfig.isPresent()) {
            final ConfigurationModel globalBlackDuckConfig = optionalGlobalBlackDuckConfig.get();
            final FieldAccessor fieldAccessor = new FieldAccessor(globalBlackDuckConfig.getCopyOfKeyToFieldMap());

            final Integer timeout = fieldAccessor.getInteger(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT).orElse(null);
            final String apiKey = fieldAccessor.getString(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY).orElse(null);
            if (timeout == null || apiKey == null) {
                throw new AlertException("Global config settings can not be null.");
            }
            return Optional.of(createBlackDuckServerConfig(logger, timeout, apiKey));
        }
        return Optional.empty();
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckApiToken) throws AlertException {
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setApiToken(blackDuckApiToken);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword) throws AlertException {
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = createServerConfigBuilderWithoutAuthentication(logger, blackDuckTimeout);
        blackDuckServerConfigBuilder.setUsername(blackDuckUsername);
        blackDuckServerConfigBuilder.setPassword(blackDuckPassword);

        try {
            return blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            throw new AlertException(e.getMessage(), e);
        }
    }

    public BlackDuckServerConfigBuilder createServerConfigBuilderWithoutAuthentication(final IntLogger logger, final int blackDuckTimeout) {
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setFromProperties(getBlackDuckProperties());
        blackDuckServerConfigBuilder.setLogger(logger);
        blackDuckServerConfigBuilder.setTimeout(blackDuckTimeout);
        blackDuckServerConfigBuilder.setUrl(getBlackDuckUrl().orElse(""));

        return blackDuckServerConfigBuilder;
    }

    private Properties getBlackDuckProperties() {
        final Properties properties = new Properties();
        properties.setProperty(BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "trust.cert", String.valueOf(alertProperties.getAlertTrustCertificate().orElse(false)));
        properties.setProperty(BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.host", alertProperties.getAlertProxyHost().orElse(""));
        properties.setProperty(BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.port", alertProperties.getAlertProxyPort().orElse(""));
        properties.setProperty(BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.username", alertProperties.getAlertProxyUsername().orElse(""));
        properties.setProperty(BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX + "proxy.password", alertProperties.getAlertProxyPassword().orElse(""));
        return properties;
    }

}
