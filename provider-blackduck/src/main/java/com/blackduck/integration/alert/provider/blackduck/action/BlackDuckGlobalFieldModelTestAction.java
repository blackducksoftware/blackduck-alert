/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.provider.state.ProviderProperties;
import com.blackduck.integration.alert.common.action.FieldModelTestAction;
import com.blackduck.integration.alert.common.exception.AlertFieldException;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.builder.BuilderStatus;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.rest.client.ConnectionResult;
import com.blackduck.integration.rest.exception.IntegrationRestException;

@Component
public class BlackDuckGlobalFieldModelTestAction extends FieldModelTestAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BlackDuckGlobalFieldModelTestAction(BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        String apiToken = registeredFieldValues.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        String url = registeredFieldValues.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        String timeout = registeredFieldValues.getStringOrEmpty(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        Long parsedConfigurationId = ProviderProperties.UNKNOWN_CONFIG_ID;

        if (StringUtils.isNotBlank(configId)) {
            try {
                parsedConfigurationId = Long.valueOf(configId);
            } catch (NumberFormatException ex) {
                throw new AlertException("Configuration id not valid.");
            }
        }

        BlackDuckProperties blackDuckProperties = blackDuckPropertiesFactory.createProperties(parsedConfigurationId, registeredFieldValues);
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
                throw AlertFieldException
                    .singleFieldError(
                        String.format("Invalid credential(s) for: %s. %s", url, failureMessage),
                        BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY,
                        "This API Key isn't valid, try a different one."
                    );
            } else if (connectionResult.getHttpStatusCode() > 0) {
                // TODO why are we throwing a non-alert exception?
                HttpUrl connectionUrl = new HttpUrl(url);
                throw new IntegrationRestException(
                    HttpMethod.GET,
                    connectionUrl,
                    connectionResult.getHttpStatusCode(),
                    String.format("Could not connect to: %s", url),
                    failureMessage,
                    errorException
                );
            }
            throw new AlertException(String.format("Could not connect to: %s. %s", url, failureMessage), errorException);
        }

        BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
        if (!blackDuckAPITokenValidator.isApiTokenValid()) {
            throw AlertFieldException
                .singleFieldError(
                    "User permission failed. Cannot read notifications from Black Duck.",
                    BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY,
                    "User permission failed. Cannot read notifications from Black Duck."
                );
        }
        return new MessageResult("Successfully connected to Black Duck server.");
    }

    public void validateBlackDuckConfiguration(BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws AlertException {
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            String errorMessage = StringUtils.join(builderStatus.getErrorMessages(), ", ");
            throw new AlertException("There were issues with the configuration: " + errorMessage);
        }
    }

}
