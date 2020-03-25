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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.system.BaseSystemValidator;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckValidator extends BaseSystemValidator {
    public static final String MISSING_BLACKDUCK_URL_ERROR = "Black Duck Provider Invalid. Black Duck URL missing.";
    public static final String BLACKDUCK_LOCALHOST_ERROR = "Black Duck Provider using localhost.";
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckValidator.class);

    public BlackDuckValidator(SystemMessageUtility systemMessageUtility) {
        super(systemMessageUtility);
    }

    public boolean validate(BlackDuckProperties blackDuckProperties) {
        boolean valid = true;
        logger.info("Validating Black Duck Provider...");
        getSystemMessageUtility().removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        getSystemMessageUtility().removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        getSystemMessageUtility().removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        try {
            Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            boolean missingUrl = addSystemMessageIfHasError(MISSING_BLACKDUCK_URL_ERROR, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING,
                blackDuckUrlOptional.isEmpty());
            if (missingUrl) {
                logger.error("  -> {}", MISSING_BLACKDUCK_URL_ERROR);
                valid = false;
            }
            if (blackDuckUrlOptional.isPresent()) {
                String blackDuckUrlString = blackDuckUrlOptional.get();
                Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> Black Duck Provider URL found validating: {}", blackDuckUrlString);
                logger.debug("  -> Black Duck Provider Timeout: {}", timeout);
                URL blackDuckUrl = new URL(blackDuckUrlString);

                boolean localHostError = addSystemMessageIfHasError(BLACKDUCK_LOCALHOST_ERROR, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST,
                    "localhost".equals(blackDuckUrl.getHost()));
                if (localHostError) {
                    logger.warn("  -> {}", BLACKDUCK_LOCALHOST_ERROR);
                }
                IntLogger intLogger = new Slf4jIntLogger(logger);
                Optional<BlackDuckServerConfig> blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfig(intLogger);
                if (blackDuckServerConfig.isPresent()) {
                    boolean canConnect = blackDuckServerConfig.get().canConnect(intLogger);
                    if (canConnect) {
                        logger.info("  -> Black Duck Provider Valid!");
                    } else {
                        final String message = "Can not connect to the Black Duck server with the current configuration.";
                        connectivityWarning(message);
                        valid = false;
                    }
                } else {
                    final String message = "The Black Duck configuration is not valid.";
                    connectivityWarning(message);
                    valid = false;
                }
            }
        } catch (MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> Black Duck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> Black Duck Provider Stack Trace: ", ex);
            valid = false;
        }
        return valid;
    }

    private void connectivityWarning(String message) {
        logger.warn(message);
        getSystemMessageUtility().addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
    }
}
