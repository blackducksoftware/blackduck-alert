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
package com.synopsys.integration.alert.provider.blackduck;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckValidator extends BaseSystemValidator {
    public static final String MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT = "Black Duck configuration '%s' is invalid. Black Duck URL missing.";
    public static final String MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT = "Black Duck configuration is invalid. Black Duck configurations missing.";
    public static final String BLACKDUCK_LOCALHOST_ERROR_FORMAT = "Black Duck configuration '%s' is using localhost.";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckValidator.class);

    public BlackDuckValidator(SystemMessageUtility systemMessageUtility) {
        super(systemMessageUtility);
    }

    public boolean validate(BlackDuckProperties blackDuckProperties) {
        boolean valid = true;
        String configName = blackDuckProperties.getConfigName();
        logger.info("Validating Black Duck configuration '{}'...", configName);

        removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING);
        try {
            Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (ProviderProperties.UNKNOWN_CONFIG_NAME.equals(configName)) {
                addSystemMessageForError(MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING, true);
            } else {
                removeOldConfigMessages(configName);
                removeOldConfigMessages(blackDuckProperties, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
                String errorMessage = String.format(MISSING_BLACKDUCK_URL_ERROR_W_CONFIG_FORMAT, configName);

                boolean missingUrl = addSystemMessageForError(errorMessage, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING,
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
                    boolean localHostError = addSystemMessageForError(String.format(BLACKDUCK_LOCALHOST_ERROR_FORMAT, configName), SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST,
                        "localhost".equals(blackDuckUrl.getHost()));
                    if (localHostError) {
                        logger.warn("  -> {}", String.format(BLACKDUCK_LOCALHOST_ERROR_FORMAT, configName));
                    }
                    IntLogger intLogger = new Slf4jIntLogger(logger);
                    Optional<BlackDuckServerConfig> blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfig(intLogger);
                    if (blackDuckServerConfig.isPresent()) {
                        boolean canConnect = blackDuckServerConfig.get().canConnect(intLogger);
                        if (canConnect) {
                            logger.info("  -> Black Duck configuration '{}' is Valid!", configName);
                        } else {
                            String message = String.format("Can not connect to the Black Duck server with the configuration '%s'.", configName);
                            connectivityWarning(message);
                            valid = false;
                        }
                    } else {
                        String message = String.format("The Black Duck configuration '%s' is not valid.", configName);
                        connectivityWarning(message);
                        valid = false;
                    }
                }
            }
        } catch (MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> Black Duck configuration '{}' is invalid; cause: {}", configName, ex.getMessage());
            logger.debug(String.format("  -> Black Duck configuration '%s' Stack Trace: ", configName), ex);
            valid = false;
        }
        return valid;
    }

    // TODO Revisit the system message API perhaps add provider config id to be able to delete messages for provider config
    // TODO Remove this in 8.0.0
    @Deprecated
    private void removeOldConfigMessages(String configName) {
        List<SystemMessageModel> messagesToDelete = getSystemMessageUtility().getSystemMessages().stream()
                                                        .filter(message -> message.getContent().contains(String.format("'%s'", configName)))
                                                        .collect(Collectors.toList());
        getSystemMessageUtility().deleteSystemMessages(messagesToDelete);
    }

    private void removeOldConfigMessages(BlackDuckProperties properties, SystemMessageType... systemMessageTypes) {
        for (SystemMessageType systemMessageType : systemMessageTypes) {
            removeSystemMessagesByTypeString(createProviderSystemMessageType(properties, systemMessageType));
        }
    }

    private String createProviderSystemMessageType(BlackDuckProperties properties, SystemMessageType systemMessageType) {
        return String.format("%d_%s", properties.getConfigId(), systemMessageType.name());
    }

    private void connectivityWarning(String message) {
        logger.warn(message);
        getSystemMessageUtility().addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
    }
}
