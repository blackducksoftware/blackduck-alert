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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.DescriptorActionApi;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerVerifier;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.BuilderStatus;

@Component
public class BlackDuckProviderDescriptorActionApi extends DescriptorActionApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckProviderDescriptorActionApi(final BlackDuckTypeConverter databaseContentConverter, final BlackDuckRepositoryAccessor repositoryAccessor, final BlackDuckProviderStartupComponent startupComponent,
            final BlackDuckProperties blackDuckProperties) {
        super(databaseContentConverter, repositoryAccessor, startupComponent);
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    public void validateConfig(final Config config, final Map<String, String> fieldErrors) {
        final BlackDuckConfig blackDuckConfig = (BlackDuckConfig) config;
        if (StringUtils.isNotBlank(blackDuckConfig.getBlackDuckTimeout()) && !StringUtils.isNumeric(blackDuckConfig.getBlackDuckTimeout())) {
            fieldErrors.put("blackDuckTimeout", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(blackDuckConfig.getBlackDuckApiKey())) {
            if (blackDuckConfig.getBlackDuckApiKey().length() < 64 || blackDuckConfig.getBlackDuckApiKey().length() > 256) {
                fieldErrors.put("blackDuckApiKey", "Invalid Black Duck API Key.");
            }
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final BlackDuckConfig blackDuckConfig = (BlackDuckConfig) testConfig.getRestModel();
        final String apiToken = blackDuckConfig.getBlackDuckApiKey();
        final String url = blackDuckConfig.getBlackDuckUrl();

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = blackDuckProperties.createServerConfigBuilderWithoutAuthentication(intLogger, NumberUtils.toInt(blackDuckConfig.getBlackDuckTimeout(), 300));
        blackDuckServerConfigBuilder.setApiToken(apiToken);
        blackDuckServerConfigBuilder.setUrl(url);

        validateBlackDuckConfiguration(blackDuckServerConfigBuilder);

        final BlackDuckRestConnection restConnection = createRestConnection(blackDuckServerConfigBuilder);
        final BlackDuckServerVerifier blackDuckServerVerifier = new BlackDuckServerVerifier();
        blackDuckServerVerifier.verifyIsBlackDuckServer(restConnection.getBaseUrl(), restConnection.getProxyInfo(), restConnection.isAlwaysTrustServerCertificate(), restConnection.getTimeout());

        final Request authRequest = RequestFactory.createCommonGetRequest(url);
        try (final Response response = restConnection.execute(authRequest)) {
            if (response.isStatusCodeError()) {
                throw new IntegrationRestException(response.getStatusCode(), response.getStatusMessage(), response.getContentString(), "Connection error");
            }
        } catch (final IOException ioException) {
            throw new IntegrationException(ioException.getMessage(), ioException);
        }
    }

    public void validateBlackDuckConfiguration(final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws AlertFieldException {
        final BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            final String errorMessage = StringUtils.join(builderStatus.getErrorMessages(), ", ");
            // FIXME the following will require UI changes
            // throw new AlertFieldException("There were issues with the configuration: " + errorMessage);
        }
    }

    private BlackDuckRestConnection createRestConnection(final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException {
        final BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        return blackDuckServerConfig.createRestConnection(blackDuckServerConfigBuilder.getLogger());
    }

}
