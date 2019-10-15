package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public abstract class CustomButtonField extends ConfigField {
    private String buttonLabel;
    private String endpoint;
    private Set<String> returnedDataFieldKeys;

    public CustomButtonField(final String key, final String label, final String description, final FieldType type, final boolean required, final boolean sensitive, final String buttonLabel, final String endpoint) {
        super(key, label, description, type, required, sensitive);
        this.buttonLabel = buttonLabel;
        this.endpoint = endpoint;
        this.returnedDataFieldKeys = new HashSet<>();
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Set<String> getReturnedDataFieldKeys() {
        return returnedDataFieldKeys;
    }

    public CustomButtonField addDataFieldKey(String key) {
        returnedDataFieldKeys.add(key);
        return this;
    }

}
