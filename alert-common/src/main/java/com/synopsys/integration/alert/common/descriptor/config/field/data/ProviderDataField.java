package com.synopsys.integration.alert.common.descriptor.config.field.data;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigValidationFunction;

public abstract class ProviderDataField extends ConfigField {
    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, boolean readOnly, String panel, String header,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, readOnly, panel, header, validationFunction);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, String panel) {
        super(key, label, description, type, required, sensitive, panel);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive, String panel,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, panel, validationFunction);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive) {
        super(key, label, description, type, required, sensitive);
    }

    public ProviderDataField(String key, String label, String description, String type, boolean required, boolean sensitive,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, type, required, sensitive, validationFunction);
    }

}
