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
package com.synopsys.integration.alert.workflow.startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.service.model.HubServerVerifier;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class SystemValidator {
    private static final Logger logger = LoggerFactory.getLogger(SystemValidator.class);
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final EncryptionUtility encryptionUtility;
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;

    @Autowired
    public SystemValidator(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final EncryptionUtility encryptionUtility, final SystemStatusUtility systemStatusUtility,
        final SystemMessageUtility systemMessageUtility) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.encryptionUtility = encryptionUtility;
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
    }

    public boolean validate() {
        return validate(new HashMap<>());
    }

    public boolean validate(final Map<String, String> fieldErrors) {
        logger.info("----------------------------------------");
        logger.info("Validating system configuration....");
        final boolean encryptionValid = validateEncryptionProperties(fieldErrors);
        final boolean providersValid = validateProviders(fieldErrors);
        final boolean valid = encryptionValid && providersValid;
        logger.info("System configuration valid: {}", valid);
        logger.info("----------------------------------------");
        systemStatusUtility.setSystemInitialized(valid);
        return valid;
    }

    public boolean validateEncryptionProperties(final Map<String, String> fieldErrors) {
        final boolean valid;
        if (encryptionUtility.isInitialized()) {
            logger.info("Encryption utilities: Initialized");
            valid = true;
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        } else {
            logger.error("Encryption utilities: Not Initialized");
            if (!encryptionUtility.isPasswordSet()) {
                final String errorMessage = "Encryption password missing";
                fieldErrors.put("globalEncryptionPassword", errorMessage);
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
            }

            if (!encryptionUtility.isGlobalSaltSet()) {
                final String errorMessage = "Encryption global salt missing";
                fieldErrors.put("globalEncryptionSalt", errorMessage);
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
            }
            valid = false;
        }
        return valid;
    }

    public boolean validateProviders(final Map<String, String> fieldErrors) {
        final boolean valid;
        logger.info("Validating configured providers: ");
        valid = validateBlackDuckProvider(fieldErrors);
        return valid;
    }

    // TODO add this validation to provider descriptors so we can run this when it's defined
    public boolean validateBlackDuckProvider(final Map<String, String> fieldErrors) {
        logger.info("Validating BlackDuck Provider...");
        boolean valid = true;
        try {
            final HubServerVerifier verifier = new HubServerVerifier();
            final ProxyInfoBuilder proxyBuilder = createProxyInfoBuilder();
            final ProxyInfo proxyInfo = proxyBuilder.build();
            final Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (!blackDuckUrlOptional.isPresent()) {
                logger.error("  -> BlackDuck Provider Invalid; cause: Black Duck URL missing...");
                final String errorMessage = "BlackDuck Provider invalid: URL missing";
                fieldErrors.put("blackDuckProviderUrl", errorMessage);
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
                valid = false;
            } else {
                final String blackDuckUrlString = blackDuckUrlOptional.get();
                final Boolean trustCertificate = BooleanUtils.toBoolean(alertProperties.getAlertTrustCertificate().orElse(false));
                final Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> BlackDuck Provider URL found validating: {}", blackDuckUrlString);
                logger.debug("  -> BlackDuck Provider Trust Cert: {}", trustCertificate);
                logger.debug("  -> BlackDuck Provider Timeout: {}", timeout);
                final URL blackDuckUrl = new URL(blackDuckUrlString);
                if ("localhost".equals(blackDuckUrl.getHost())) {
                    logger.warn("  -> BlackDuck Provider Using localhost...");
                    final String blackDuckWebServerHost = blackDuckProperties.getPublicBlackDuckWebserverHost().orElse("");
                    logger.warn("  -> BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to {}", blackDuckWebServerHost);
                    systemMessageUtility.addSystemMessage("BlackDuck Provider Using localhost", SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
                }
                verifier.verifyIsHubServer(blackDuckUrl, proxyInfo, trustCertificate, timeout);
                logger.info("  -> BlackDuck Provider Valid!");
            }
        } catch (final MalformedURLException | IntegrationException ex) {
            logger.error("  -> BlackDuck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> BlackDuck Provider Stack Trace: ", ex);
            fieldErrors.put("blackDuckProviderUrl", "BlackDuck Provider Url invalid.");
            systemMessageUtility.addSystemMessage("BlackDuck Provider invalid: " + ex.getMessage(), SystemMessageSeverity.ERROR, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
            valid = false;
        }

        if (valid) {
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        }
        return valid;
    }

    private ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        final Optional<String> alertProxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> alertProxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> alertProxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> alertProxyPassword = alertProperties.getAlertProxyPassword();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            proxyBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            proxyBuilder.setPassword(alertProxyPassword.get());
        }
        return proxyBuilder;
    }
}
