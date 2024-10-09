/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.SystemMessageSeverity;
import com.blackduck.integration.alert.common.enumeration.SystemMessageType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.system.BaseSystemValidator;

@Component
public class ProviderConfigMissingValidator extends BaseSystemValidator {
    public static final String MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT = "Black Duck configuration is invalid. Black Duck configurations missing.";

    private final List<Provider> providers;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public ProviderConfigMissingValidator(SystemMessageAccessor systemMessageAccessor, List<Provider> providers,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        super(systemMessageAccessor);
        this.providers = providers;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    public void validate() {
        removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING);
        for (Provider provider : providers) {
            List<ConfigurationModel> configurations = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
            boolean emptyConfiguration = isConfigurationsEmpty(configurations);
            if (emptyConfiguration) {
                addSystemMessageForError(MISSING_BLACKDUCK_CONFIG_ERROR_FORMAT, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONFIGURATION_MISSING, true);
            }
        }
    }

    private boolean isConfigurationsEmpty(List<ConfigurationModel> configurations) {
        if (configurations.isEmpty()) {
            return true;
        }
        return configurations.stream()
                   .map(ConfigurationModel::getCopyOfFieldList)
                   .allMatch(List::isEmpty);
    }

}
