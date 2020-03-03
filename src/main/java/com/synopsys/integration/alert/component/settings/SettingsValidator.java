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
package com.synopsys.integration.alert.component.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class SettingsValidator {
    private static final Logger logger = LoggerFactory.getLogger(SettingsValidator.class);
    private EncryptionUtility encryptionUtility;
    private SystemMessageUtility systemMessageUtility;

    @Autowired
    public SettingsValidator(EncryptionUtility encryptionUtility, SystemMessageUtility systemMessageUtility) {
        this.encryptionUtility = encryptionUtility;
        this.systemMessageUtility = systemMessageUtility;
    }

    public Map<String, String> validateEncryption() {
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);

        Map<String, String> fieldErrors = new HashMap<>();
        if (!encryptionUtility.isInitialized()) {
            logger.error("Encryption utilities: Not Initialized");

            validationCheck(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_PWD, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR, !encryptionUtility.isPasswordSet())
                .ifPresent(error -> fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_PWD, error));
            validationCheck(SettingsDescriptor.FIELD_ERROR_ENCRYPTION_GLOBAL_SALT, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR, !encryptionUtility.isGlobalSaltSet())
                .ifPresent(error -> fieldErrors.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, error));
            return fieldErrors;
        }

        logger.info("Encryption utilities: Initialized");
        return fieldErrors;
    }

    private Optional<String> validationCheck(String errorMessage, SystemMessageType messageType, boolean validationCheck) {
        if (validationCheck) {
            systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, messageType);
            return Optional.of(errorMessage);
        }

        return Optional.empty();
    }

}
