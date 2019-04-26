/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.actions;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerVerifier;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

@Component
public class BlackDuckGlobalTestAction extends TestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckGlobalTestAction(final BlackDuckProperties blackDuckProperties) {
        super(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL);
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();

        final String apiToken = fieldAccessor.getString(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY).orElse("");
        final String url = fieldAccessor.getString(BlackDuckDescriptor.KEY_BLACKDUCK_URL).orElse("");
        final String timeout = fieldAccessor.getString(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT).orElse("");
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = blackDuckProperties.createServerConfigBuilderWithoutAuthentication(intLogger, NumberUtils.toInt(timeout, 300));
        blackDuckServerConfigBuilder.setApiToken(apiToken);
        blackDuckServerConfigBuilder.setUrl(url);

        validateBlackDuckConfiguration(blackDuckServerConfigBuilder);

        final BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        final BlackDuckServerVerifier blackDuckServerVerifier = new BlackDuckServerVerifier();
        blackDuckServerVerifier.verifyIsBlackDuckServer(blackDuckServerConfig.getBlackDuckUrl(), blackDuckServerConfig.getProxyInfo(), blackDuckServerConfig.isAlwaysTrustServerCertificate(), blackDuckServerConfig.getTimeout());

        final BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(blackDuckServerConfigBuilder.getLogger());
        final Request authRequest = RequestFactory.createCommonGetRequest(url);
        try (final Response response = blackDuckHttpClient.execute(authRequest)) {
            if (response.isStatusCodeError()) {
                throw new IntegrationRestException(response.getStatusCode(), response.getStatusMessage(), response.getContentString(), "Connection error");
            }
        } catch (final IOException ioException) {
            throw new IntegrationException(ioException.getMessage(), ioException);
        }
    }

    public void validateBlackDuckConfiguration(final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws AlertException {
        final BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            final String errorMessage = StringUtils.join(builderStatus.getErrorMessages(), ", ");
            throw new AlertException("There were issues with the configuration: " + errorMessage);
        }
    }

}
