/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

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
                IntLogger intLogger = new Slf4jIntLogger(logger);
                BlackDuckServerConfig blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfig(intLogger);

                boolean canConnect = blackDuckServerConfig.canConnect(intLogger);
                if (canConnect) {
                    logger.info("  -> Black Duck configuration '{}' is Valid!", configName);
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

            BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
            if (!blackDuckAPITokenValidator.isApiTokenValid()) {
                connectivityWarning(blackDuckProperties, BLACKDUCK_API_PERMISSION_FORMAT);
                valid = false;
            }
        } catch (MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> Black Duck configuration '{}' is invalid; cause: {}", configName, ex.getMessage());
            logger.debug(String.format("  -> Black Duck configuration '%s' Stack Trace: ", configName), ex);
            valid = false;
        }
        return valid;
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
