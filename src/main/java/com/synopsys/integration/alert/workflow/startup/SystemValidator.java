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
package com.synopsys.integration.alert.workflow.startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.service.model.BlackDuckServerVerifier;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class SystemValidator {
    private static final Logger logger = LoggerFactory.getLogger(SystemValidator.class);
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final EncryptionUtility encryptionUtility;
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;
    private final DefaultUserAccessor userAccessor;
    private final ProxyManager proxyManager;

    @Autowired
    public SystemValidator(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final EncryptionUtility encryptionUtility, final SystemStatusUtility systemStatusUtility,
        final SystemMessageUtility systemMessageUtility, final DefaultUserAccessor userAccessor, final ProxyManager proxyManager) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.encryptionUtility = encryptionUtility;
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
        this.userAccessor = userAccessor;
        this.proxyManager = proxyManager;
    }

    public boolean validate() {
        return validate(new HashMap<>());
    }

    public boolean validate(final Map<String, String> fieldErrors) {
        logger.info("----------------------------------------");
        logger.info("Validating system configuration....");

        final boolean defaultUserSettingsValid = validateDefaultUser(fieldErrors);
        final boolean encryptionValid = validateEncryptionProperties(fieldErrors);
        final boolean proxyValid = validateProxyProperties();
        final boolean providersValid = validateProviders();
        final boolean valid = defaultUserSettingsValid && encryptionValid && proxyValid && providersValid;
        logger.info("System configuration valid: {}", valid);
        logger.info("----------------------------------------");
        systemStatusUtility.setSystemInitialized(valid);
        return valid;
    }

    public boolean validateDefaultUser(final Map<String, String> fieldErrors) {
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
        final boolean adminUserEmailValid = validateDefaultAdminEmailSet(fieldErrors);
        final boolean adminUserPasswordValid = validateDefaultAdminPasswordSet(fieldErrors);
        return adminUserEmailValid && adminUserPasswordValid;
    }

    public boolean validateDefaultAdminEmailSet(final Map<String, String> fieldErrors) {
        final Optional<String> emailAddress = userAccessor
                                                  .getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER)
                                                  .map(UserModel::getEmailAddress)
                                                  .filter(StringUtils::isNotBlank);
        final boolean valid = emailAddress.isPresent();

        if (!valid) {
            final String errorMessage = SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_EMAIL;
            fieldErrors.put(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_EMAIL, errorMessage);
            systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
        }
        return valid;
    }

    public boolean validateDefaultAdminPasswordSet(final Map<String, String> fieldErrors) {
        final Optional<String> passwordSet = userAccessor
                                                 .getUser(DefaultUserAccessor.DEFAULT_ADMIN_USER)
                                                 .map(UserModel::getPassword)
                                                 .filter(StringUtils::isNotBlank);
        final boolean valid = passwordSet.isPresent();
        if (!valid) {
            final String errorMessage = SettingsDescriptor.FIELD_ERROR_DEFAULT_USER_PWD;
            fieldErrors.put(SettingsDescriptor.KEY_DEFAULT_SYSTEM_ADMIN_PWD, errorMessage);
            systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.DEFAULT_ADMIN_USER_ERROR);
        }
        return valid;
    }

    public boolean validateEncryptionProperties(final Map<String, String> fieldErrors) {
        final boolean valid;
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        if (encryptionUtility.isInitialized()) {
            logger.info("Encryption utilities: Initialized");
            valid = true;
        } else {
            logger.error("Encryption utilities: Not Initialized");
            if (!encryptionUtility.isPasswordSet()) {
                final String errorMessage = SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PWD;
                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_PWD, errorMessage);
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
            }

            if (!encryptionUtility.isGlobalSaltSet()) {
                final String errorMessage = SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT;
                fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, errorMessage);
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
            }
            valid = false;
        }
        return valid;
    }

    public boolean validateProxyProperties() {
        boolean valid = true;
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.PROXY_CONFIGURATION_ERROR);
        try {
            proxyManager.createProxyInfo();
        } catch (final IllegalArgumentException e) {
            valid = false;
            logger.error("  -> Proxy Invalid; cause: {}", e.getMessage());
            logger.debug("  -> Proxy Stack Trace: ", e);
            systemMessageUtility.addSystemMessage("Proxy invalid: " + e.getMessage(), SystemMessageSeverity.WARNING, SystemMessageType.PROXY_CONFIGURATION_ERROR);
        }
        return valid;
    }

    public boolean validateProviders() {
        final boolean valid;
        logger.info("Validating configured providers: ");
        valid = validateBlackDuckProvider();
        return valid;
    }

    // TODO add this validation to provider descriptors so we can run this when it's defined (Or delete it entirely as we no longer require a BD setup)
    public boolean validateBlackDuckProvider() {
        logger.info("Validating BlackDuck Provider...");
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        try {
            final BlackDuckServerVerifier verifier = new BlackDuckServerVerifier();
            ProxyInfo proxyInfo;
            try {
                proxyInfo = proxyManager.createProxyInfo();
            } catch (final IllegalArgumentException e) {
                proxyInfo = ProxyInfo.NO_PROXY_INFO;
            }

            final Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (blackDuckUrlOptional.isEmpty()) {
                logger.error("  -> BlackDuck Provider Invalid; cause: Black Duck URL missing...");
                final String errorMessage = "BlackDuck Provider invalid: URL missing";
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
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
                    systemMessageUtility.addSystemMessage("BlackDuck Provider Using localhost", SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
                }
                verifier.verifyIsBlackDuckServer(blackDuckUrl, proxyInfo, trustCertificate, timeout);
                logger.info("  -> BlackDuck Provider Valid!");
            }
        } catch (final MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> BlackDuck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> BlackDuck Provider Stack Trace: ", ex);
            systemMessageUtility.addSystemMessage("BlackDuck Provider invalid: " + ex.getMessage(), SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        }
        return true;
    }
}
