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

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.provider.ProviderValidator;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

public class BlackDuckValidator extends ProviderValidator {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckValidator.class);

    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final SystemMessageUtility systemMessageUtility;

    public BlackDuckValidator(AlertProperties alertProperties, BlackDuckProperties blackDuckProperties, SystemMessageUtility systemMessageUtility) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.systemMessageUtility = systemMessageUtility;
    }

    @Override
    public boolean validate() {
        logger.info("Validating Black Duck Provider...");
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        try {
            Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (blackDuckUrlOptional.isEmpty()) {
                logger.error("  -> Black Duck Provider Invalid; cause: Black Duck URL missing...");
                final String errorMessage = "Black Duck Provider invalid: URL missing";
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            } else {
                String blackDuckUrlString = blackDuckUrlOptional.get();
                Boolean trustCertificate = BooleanUtils.toBoolean(alertProperties.getAlertTrustCertificate().orElse(false));
                Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> Black Duck Provider URL found validating: {}", blackDuckUrlString);
                logger.debug("  -> Black Duck Provider Trust Cert: {}", trustCertificate);
                logger.debug("  -> Black Duck Provider Timeout: {}", timeout);
                URL blackDuckUrl = new URL(blackDuckUrlString);
                if ("localhost".equals(blackDuckUrl.getHost())) {
                    logger.warn("  -> Black Duck Provider Using localhost...");
                    systemMessageUtility.addSystemMessage("Black Duck Provider Using localhost", SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
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
                    }
                } else {
                    final String message = "The Black Duck configuration is not valid.";
                    connectivityWarning(message);
                }
            }
        } catch (MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> Black Duck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> Black Duck Provider Stack Trace: ", ex);
        }
        return true;
    }

    private void connectivityWarning(String message) {
        logger.warn(message);
        systemMessageUtility.addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
    }
}
