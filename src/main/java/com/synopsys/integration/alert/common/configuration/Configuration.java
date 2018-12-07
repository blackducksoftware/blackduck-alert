package com.synopsys.integration.alert.common.configuration;

import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;

public class Configuration {
    private final Long id;
    private final FieldAccessor fieldAccessor;

    public Configuration(final ConfigurationModel configurationModel) {
        fieldAccessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());

        id = configurationModel.getConfigurationId();
    }

    public Long getId() {
        return id;
    }

    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }
}
