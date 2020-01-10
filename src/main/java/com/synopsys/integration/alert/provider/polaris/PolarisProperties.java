/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.polaris;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class PolarisProperties extends ProviderProperties {
    public static final Integer DEFAULT_TIMEOUT = 300;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final Gson gson;

    @Autowired
    public PolarisProperties(PolarisProviderKey polarisProviderKey, AlertProperties alertProperties, ConfigurationAccessor configurationAccessor, ProxyManager proxyManager, Gson gson) {
        super(polarisProviderKey, configurationAccessor);
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.gson = gson;
    }

    public Optional<String> getUrl() {
        return createFieldAccessor()
                   .getString(PolarisDescriptor.KEY_POLARIS_URL)
                   .filter(StringUtils::isNotBlank);
    }

    public Integer getTimeout() {
        return createFieldAccessor()
                   .getInteger(PolarisDescriptor.KEY_POLARIS_TIMEOUT)
                   .orElse(DEFAULT_TIMEOUT);
    }

    public Optional<AccessTokenPolarisHttpClient> createPolarisHttpClientSafely(final Logger logger) {
        return createPolarisHttpClientSafely(new Slf4jIntLogger(logger));
    }

    public Optional<AccessTokenPolarisHttpClient> createPolarisHttpClientSafely(final IntLogger intLogger) {
        try {
            return Optional.of(createPolarisHttpClient(intLogger));
        } catch (final IntegrationException e) {
            return Optional.empty();
        }
    }

    public AccessTokenPolarisHttpClient createPolarisHttpClient(final Logger logger) throws IntegrationException {
        return createPolarisHttpClient(new Slf4jIntLogger(logger));
    }

    public AccessTokenPolarisHttpClient createPolarisHttpClient(final IntLogger intLogger) throws IntegrationException {
        return createPolarisServerConfig(intLogger).createPolarisHttpClient(intLogger);
    }

    public PolarisServicesFactory createPolarisServicesFactory(final IntLogger intLogger) throws IntegrationException {
        return createPolarisServerConfig(intLogger).createPolarisServicesFactory(intLogger);
    }

    public PolarisServerConfig createPolarisServerConfig(final IntLogger intLogger) throws IntegrationException {
        final String errorFormat = "The field %s cannot be blank";
        final String baseUrl = getUrl()
                                   .orElseThrow(() -> new IntegrationException(String.format(errorFormat, "baseUrl")));
        final String accessToken = getAccessToken()
                                       .orElseThrow(() -> new IntegrationException(String.format(errorFormat, "accessToken")));
        final Integer timeout = getTimeout();

        final PolarisServerConfigBuilder polarisServerConfigBuilder = createInitialPolarisServerConfigBuilder(intLogger);
        polarisServerConfigBuilder.setUrl(baseUrl);
        polarisServerConfigBuilder.setAccessToken(accessToken);
        polarisServerConfigBuilder.setTimeoutInSeconds(timeout);

        final BuilderStatus builderStatus = polarisServerConfigBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            throw new IntegrationException(builderStatus.getFullErrorMessage());
        }
        return polarisServerConfigBuilder.build();
    }

    public PolarisServerConfigBuilder createInitialPolarisServerConfigBuilder(final IntLogger logger) {
        final PolarisServerConfigBuilder builder = new PolarisServerConfigBuilder();
        builder.setLogger(logger);
        builder.setGson(gson);
        builder.setTrustCert(alertProperties.getAlertTrustCertificate().orElse(Boolean.FALSE));

        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        final Optional<String> optionalProxyHost = proxyInfo.getHost().filter(StringUtils::isNotBlank);
        if (optionalProxyHost.isPresent()) {
            builder.setProxyHost(optionalProxyHost.get());
            builder.setProxyPort(proxyInfo.getPort());
            final Optional<Credentials> optionalProxyCredentials = proxyInfo.getProxyCredentials();
            if (optionalProxyCredentials.isPresent()) {
                final Credentials proxyCredentials = optionalProxyCredentials.get();
                builder.setProxyUsername(proxyCredentials.getUsername().orElse(null));
                builder.setProxyUsername(proxyCredentials.getPassword().orElse(null));
            }
        }

        return builder;
    }

    private Optional<String> getAccessToken() {
        return createFieldAccessor()
                   .getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)
                   .filter(StringUtils::isNotBlank);
    }

}
