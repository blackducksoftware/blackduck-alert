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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.provider.ProviderValidator;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;

@Component
@Order(2)
public class SystemValidator extends StartupComponent {
    private static final Logger logger = LoggerFactory.getLogger(SystemValidator.class);
    private final List<ProviderValidator> providerValidators;
    private final EncryptionUtility encryptionUtility;
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;
    private final DefaultUserAccessor userAccessor;
    private final ProxyManager proxyManager;

    @Autowired
    public SystemValidator(final List<ProviderValidator> providerValidators, final EncryptionUtility encryptionUtility, final SystemStatusUtility systemStatusUtility, final SystemMessageUtility systemMessageUtility,
        final DefaultUserAccessor userAccessor, final ProxyManager proxyManager) {
        this.providerValidators = providerValidators;
        this.encryptionUtility = encryptionUtility;
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
        this.userAccessor = userAccessor;
        this.proxyManager = proxyManager;
    }

    @Override
    protected void initialize() {
        validate();
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
        boolean valid = true;
        logger.info("Validating configured providers: ");
        for (final ProviderValidator providerValidator : providerValidators) {
            valid = valid && providerValidator.validate();
        }
        return valid;
    }

}
