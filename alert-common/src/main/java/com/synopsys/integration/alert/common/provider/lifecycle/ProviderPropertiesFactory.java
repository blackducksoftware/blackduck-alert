/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.lifecycle;

import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;

public abstract class ProviderPropertiesFactory<T extends ProviderProperties> {
    private final ConfigurationAccessor configurationAccessor;

    public ProviderPropertiesFactory(ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    public Optional<T> createProperties(Long configId) {
        return configurationAccessor.getConfigurationById(configId).map(this::createProperties);
    }

    public T createProperties(ConfigurationModel configurationModel) {
        return createProperties(configurationModel.getConfigurationId(), new FieldUtility(configurationModel.getCopyOfKeyToFieldMap()));
    }

    public abstract T createProperties(Long configId, FieldUtility fieldUtility);

}
