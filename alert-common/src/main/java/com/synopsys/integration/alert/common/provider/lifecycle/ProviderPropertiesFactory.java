package com.synopsys.integration.alert.common.provider.lifecycle;

import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderProperties;

public abstract class ProviderPropertiesFactory<T extends ProviderProperties> {
    public T createProperties(ConfigurationModel configurationModel) {
        return createProperties(configurationModel.getConfigurationId(), new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap()));
    }

    public abstract T createProperties(Long configId, FieldAccessor fieldAccessor);
}
