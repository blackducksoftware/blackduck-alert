/**
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.component.settings.SettingsValidator;
import com.synopsys.integration.alert.component.users.UserSystemValidator;

@Component
@Order(30)
public class SystemMessageInitializer extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(SystemMessageInitializer.class);
    private final List<Provider> providers;
    private final SettingsValidator settingsValidator;
    private final ConfigurationAccessor configurationAccessor;
    private final UserSystemValidator userSystemValidator;
    private final SystemMessageAccessor systemMessageAccessor;

    @Autowired
    public SystemMessageInitializer(List<Provider> providers, SettingsValidator settingsValidator, ConfigurationAccessor configurationAccessor, UserSystemValidator userSystemValidator, SystemMessageAccessor systemMessageAccessor) {
        this.providers = providers;
        this.settingsValidator = settingsValidator;
        this.configurationAccessor = configurationAccessor;
        this.userSystemValidator = userSystemValidator;
        this.systemMessageAccessor = systemMessageAccessor;
    }

    @Override
    protected void initialize() {
        validate();
    }

    public boolean validate() {
        logger.info("----------------------------------------");
        logger.info("Validating system configuration....");

        clearOldMessages();
        boolean defaultAdminValid = userSystemValidator.validateDefaultAdminUser();
        boolean encryptionValid = settingsValidator.validateEncryption();
        boolean providersValid = validateProviders();
        boolean valid = defaultAdminValid && encryptionValid && providersValid;
        logger.info("System configuration valid: {}", valid);
        logger.info("----------------------------------------");
        return valid;
    }

    private void clearOldMessages() {
        List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessages();
        systemMessageAccessor.deleteSystemMessages(messages);
    }

    public boolean validateProviders() {
        boolean valid = true;
        logger.info("Validating configured providers: ");
        for (Provider provider : providers) {
            List<ConfigurationModel> configurations = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
            valid = configurations.stream()
                        .filter(model -> !model.getCopyOfFieldList().isEmpty())
                        .allMatch(provider::validate);
        }
        return valid;
    }

}
