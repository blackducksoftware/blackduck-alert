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
package com.synopsys.integration.alert.provider.blackduck.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.ConnectionResult;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class BlackDuckGlobalTestAction extends TestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckGlobalTestAction(BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    public MessageResult testConfig(String configId, String description, FieldAccessor fieldAccessor) throws IntegrationException {
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        String apiToken = fieldAccessor.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        String url = fieldAccessor.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        String timeout = fieldAccessor.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = blackDuckProperties.createServerConfigBuilderWithoutAuthentication(intLogger, NumberUtils.toInt(timeout, 300));
        blackDuckServerConfigBuilder.setApiToken(apiToken);
        blackDuckServerConfigBuilder.setUrl(url);

        validateBlackDuckConfiguration(blackDuckServerConfigBuilder);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(intLogger);
        if (connectionResult.isFailure()) {
            String failureMessage = connectionResult.getFailureMessage().orElse("");
            Exception errorException = connectionResult.getException().orElse(null);
            if (RestConstants.UNAUTHORIZED_401 == connectionResult.getHttpStatusCode()) {
                throw AlertFieldException.singleFieldError(String.format("Invalid credential(s) for: %s. %s", url, failureMessage), BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, "This API Key isn't valid, try a different one.");
            } else if (connectionResult.getHttpStatusCode() > 0) {
                throw new IntegrationRestException(connectionResult.getHttpStatusCode(), String.format("Could not connect to: %s", url), null, failureMessage, errorException);
            }
            throw new AlertException(String.format("Could not connect to: %s. %s", url, failureMessage), errorException);
        }
        return new MessageResult("Successfully connected to BlackDuck server.");
    }

    public void validateBlackDuckConfiguration(BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws AlertException {
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            String errorMessage = StringUtils.join(builderStatus.getErrorMessages(), ", ");
            throw new AlertException("There were issues with the configuration: " + errorMessage);
        }
    }

}
