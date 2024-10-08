/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.response.Response;

@Component
public class BlackDuckSystemValidator extends BaseSystemValidator {
    public static final String MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT = "Black Duck configuration '%s' is invalid. Black Duck URL missing.";
    public static final String MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT = "Black Duck configuration is invalid. Black Duck configurations missing.";
    public static final String BLACKDUCK_LOCALHOST_ERROR_FORMAT = "Black Duck configuration '%s' is using localhost.";
    public static final String BLACKDUCK_API_PERMISSION_FORMAT = "User permission failed, cannot read notifications from Black Duck.";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckSystemValidator.class);

    public static String createProviderSystemMessageType(BlackDuckProperties properties, SystemMessageType systemMessageType) {
        return String.format("%d_%s", properties.getConfigId(), systemMessageType.name());
    }

    public BlackDuckSystemValidator(SystemMessageAccessor systemMessageAccessor) {
        super(systemMessageAccessor);
    }

    public boolean validate(BlackDuckProperties blackDuckProperties) {
        boolean valid = true;
        String configName = blackDuckProperties.getConfigName();
        logger.info("Validating Black Duck configuration '{}'...", configName);

        try {
            Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            removeOldConfigMessages(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            String errorMessage = String.format(MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT, configName);
            String urlMissingType = createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            boolean missingUrl = addSystemMessageForError(errorMessage, SystemMessageSeverity.WARNING, urlMissingType,
                blackDuckUrlOptional.isEmpty());
            if (missingUrl) {
                logger.error("  -> {}", String.format(MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT, configName));
                valid = false;
            }
            if (blackDuckUrlOptional.isPresent()) {
                String blackDuckUrlString = blackDuckUrlOptional.get();
                Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> Black Duck configuration '{}' URL found validating: {}", configName, blackDuckUrlString);
                logger.debug("  -> Black Duck configuration '{}' Timeout: {}", configName, timeout);
                URL blackDuckUrl = new URL(blackDuckUrlString);
                String localhostMissingType = createProviderSystemMessageType(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
                boolean localHostError = addSystemMessageForError(String.format(BLACKDUCK_LOCALHOST_ERROR_FORMAT, configName), SystemMessageSeverity.WARNING, localhostMissingType,
                    "localhost".equals(blackDuckUrl.getHost()));
                if (localHostError) {
                    logger.warn("  -> {}", String.format(BLACKDUCK_LOCALHOST_ERROR_FORMAT, configName));
                }

                BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
                if (canConnect(blackDuckProperties, blackDuckAPITokenValidator)) {
                    logger.info("  -> Black Duck configuration '{}' is Valid!", configName);

                    if (!blackDuckAPITokenValidator.isApiTokenValid()) {
                        connectivityWarning(blackDuckProperties, BLACKDUCK_API_PERMISSION_FORMAT);
                        valid = false;
                    }
                } else {
                    String message = String.format("Can not connect to the Black Duck server with the configuration '%s'.", configName);
                    connectivityWarning(blackDuckProperties, message);
                    valid = false;
                }
            } else {
                String message = String.format("The Black Duck configuration '%s' is not valid.", configName);
                connectivityWarning(blackDuckProperties, message);
                valid = false;
            }

        } catch (MalformedURLException | AlertRuntimeException ex) {
            logger.error("  -> Black Duck configuration '{}' is invalid; cause: {}", configName, ex.getMessage());
            logger.debug(String.format("  -> Black Duck configuration '%s' Stack Trace: ", configName), ex);
            valid = false;
        }
        return valid;
    }

    public boolean canConnect(BlackDuckProperties blackDuckProperties, BlackDuckApiTokenValidator blackDuckAPITokenValidator) {
        if (blackDuckProperties.getBlackDuckUrl().isEmpty()) {
            logger.error("Black Duck URL not configured.");
            return false;
        }

        String blackduckServerName = blackDuckProperties.getBlackDuckUrl().get();
        Response authenticationResponse;

        logger.info("  -> Attempting connection to {}", blackduckServerName);
        try {
            authenticationResponse = blackDuckAPITokenValidator.attemptAuthentication();
        } catch (IntegrationException ex) {
            logger.error("  -> Failed to make connection to {}; cause: {}", blackduckServerName, ex.getMessage());
            logger.debug(String.format("  -> Black Duck server '%s' Stack Trace: ", blackduckServerName), ex);
            return false;
        }

        if (authenticationResponse.isStatusCodeSuccess()) {
            logger.info("  -> Successfully connected to {}", blackduckServerName);
            return true;
        } else {
            logger.error("  -> Failed to make connection to {}; http status code: {}", blackduckServerName, authenticationResponse.getStatusCode());
            return false;
        }
    }

    private void removeOldConfigMessages(BlackDuckProperties properties, SystemMessageType... systemMessageTypes) {
        for (SystemMessageType systemMessageType : systemMessageTypes) {
            removeSystemMessagesByTypeString(createProviderSystemMessageType(properties, systemMessageType));
        }
    }

    private void connectivityWarning(BlackDuckProperties properties, String message) {
        logger.warn(message);
        getSystemMessageAccessor().addSystemMessage(message, SystemMessageSeverity.WARNING, createProviderSystemMessageType(properties, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY));
    }
}
