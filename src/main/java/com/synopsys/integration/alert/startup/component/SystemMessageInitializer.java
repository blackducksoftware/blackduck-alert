/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.startup.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.Provider;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.component.settings.validator.SettingsSystemValidator;
import com.synopsys.integration.alert.component.users.UserSystemValidator;

@Component
@Order(30)
public class SystemMessageInitializer extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(SystemMessageInitializer.class);
    private final List<Provider> providers;
    private final SettingsSystemValidator settingsSystemValidator;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final UserSystemValidator userSystemValidator;
    private final SystemMessageAccessor systemMessageAccessor;

    @Autowired
    public SystemMessageInitializer(List<Provider> providers, SettingsSystemValidator settingsSystemValidator, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, UserSystemValidator userSystemValidator,
        SystemMessageAccessor systemMessageAccessor) {
        this.providers = providers;
        this.settingsSystemValidator = settingsSystemValidator;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
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
        boolean encryptionValid = settingsSystemValidator.validateEncryption();
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
            List<ConfigurationModel> configurations = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
            valid = configurations.stream()
                        .filter(model -> !model.getCopyOfFieldList().isEmpty())
                        .allMatch(provider::validate);
        }
        return valid;
    }

}
