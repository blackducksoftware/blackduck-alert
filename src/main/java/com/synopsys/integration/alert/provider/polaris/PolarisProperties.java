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
package com.synopsys.integration.alert.provider.polaris;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenRestConnection;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class PolarisProperties extends ProviderProperties {
    public static final Integer DEFAULT_TIMEOUT = 300;

    private final AlertProperties alertProperties;

    @Autowired
    public PolarisProperties(final AlertProperties alertProperties, final BaseConfigurationAccessor configurationAccessor) {
        super(PolarisProvider.COMPONENT_NAME, configurationAccessor);
        this.alertProperties = alertProperties;
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

    public Optional<AccessTokenRestConnection> createRestConnectionSafely(final Logger logger) {
        return createRestConnectionSafely(new Slf4jIntLogger(logger));
    }

    public Optional<AccessTokenRestConnection> createRestConnectionSafely(final IntLogger intLogger) {
        try {
            return Optional.of(createRestConnection(intLogger));
        } catch (final IntegrationException e) {
            return Optional.empty();
        }
    }

    public AccessTokenRestConnection createRestConnection(final Logger logger) throws IntegrationException {
        return createRestConnection(new Slf4jIntLogger(logger));
    }

    public AccessTokenRestConnection createRestConnection(final IntLogger intLogger) throws IntegrationException {
        final String errorFormat = "The field %s cannot be blank";
        final String baseUrl = getUrl()
                                   .orElseThrow(() -> new IntegrationException(String.format(errorFormat, "baseUrl")));
        final String accessToken = getAccessToken()
                                       .orElseThrow(() -> new IntegrationException(String.format(errorFormat, "accessToken")));
        final Integer timeout = getTimeout();

        return createRestConnection(intLogger, baseUrl, accessToken, timeout);

    }

    public AccessTokenRestConnection createRestConnection(final IntLogger intLogger, final String baseUrl, final String accessToken, final Integer timeout) {
        final Boolean alwaysTrustCertificate = alertProperties.getAlertTrustCertificate().orElse(Boolean.FALSE);
        final ProxyInfo proxyInfo = alertProperties.createProxyInfo();

        return new AccessTokenRestConnection(intLogger, timeout, alwaysTrustCertificate, proxyInfo, baseUrl, accessToken);
    }

    private Optional<String> getAccessToken() {
        return createFieldAccessor()
                   .getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)
                   .filter(StringUtils::isNotBlank);
    }

}
