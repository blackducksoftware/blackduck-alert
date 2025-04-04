/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider.lifecycle;

import java.util.Optional;

import com.blackduck.integration.alert.api.provider.state.ProviderProperties;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

public abstract class ProviderPropertiesFactory<T extends ProviderProperties> {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    protected ProviderPropertiesFactory(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    public Optional<T> createProperties(Long configId) {
        return configurationModelConfigurationAccessor.getConfigurationById(configId).map(this::createProperties);
    }

    public T createProperties(ConfigurationModel configurationModel) {
        return createProperties(configurationModel.getConfigurationId(), new FieldUtility(configurationModel.getCopyOfKeyToFieldMap()));
    }

    public abstract T createProperties(Long configId, FieldUtility fieldUtility);

}
