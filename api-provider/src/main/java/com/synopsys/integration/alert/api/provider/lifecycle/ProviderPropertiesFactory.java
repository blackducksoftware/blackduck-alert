/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider.lifecycle;

import java.util.Optional;

import com.synopsys.integration.alert.api.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

public abstract class ProviderPropertiesFactory<T extends ProviderProperties> {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    public ProviderPropertiesFactory(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
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
