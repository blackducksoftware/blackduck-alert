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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.ProviderValidator;
import com.synopsys.integration.alert.component.settings.SettingsValidator;

@Component
@Order(40)
public class SystemMessageInitializer extends StartupComponent {
    private static final Logger logger = LoggerFactory.getLogger(SystemMessageInitializer.class);
    private final List<ProviderValidator> providerValidators;
    private SettingsValidator settingsValidator;

    @Autowired
    public SystemMessageInitializer(List<ProviderValidator> providerValidators, SettingsValidator settingsValidator) {
        this.providerValidators = providerValidators;
        this.settingsValidator = settingsValidator;
    }

    @Override
    protected void initialize() {
        validate();
    }

    public boolean validate() {
        logger.info("----------------------------------------");
        logger.info("Validating system configuration....");

        boolean defaultUserSettingsValid = settingsValidator.validateUser().isEmpty();
        boolean encryptionValid = settingsValidator.validateEncryption().isEmpty();
        boolean providersValid = validateProviders();
        boolean valid = defaultUserSettingsValid && encryptionValid && providersValid;
        logger.info("System configuration valid: {}", valid);
        logger.info("----------------------------------------");
        return valid;
    }

    public boolean validateProviders() {
        boolean valid = true;
        logger.info("Validating configured providers: ");
        for (ProviderValidator providerValidator : providerValidators) {
            valid = valid && providerValidator.validate();
        }
        return valid;
    }

}
