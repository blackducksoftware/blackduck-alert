package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointField extends ConfigField {
    private final String configureEndpoint;
    private final Boolean successBox;
    private final List<ConfigField> subFields;

    public static EndpointField create(final String key, final String label, final String description, final String configureEndpoint) {
        return new EndpointField(key, label, description, false, configureEndpoint);
    }

    public static EndpointField createRequired(final String key, final String label, final String description, final String configureEndpoint) {
        return new EndpointField(key, label, description, true, configureEndpoint);
    }

    public static EndpointField createWithSuccessBox(final String key, final String label, final String description, final String configureEndpoint) {
        return new EndpointField(key, label, description, false, configureEndpoint, true);
    }

    public static EndpointField createRequiredWithSuccessBox(final String key, final String label, final String description, final String configureEndpoint) {
        return new EndpointField(key, label, description, true, configureEndpoint, true);
    }

    private EndpointField(final String key, final String label, final String description, final boolean required, final String configureEndpoint, final Boolean successBox, final List<ConfigField> subFields) {
        super(key, label, description, FieldType.ENDPOINT.getFieldTypeName(), required, false);
        this.configureEndpoint = configureEndpoint;
        this.successBox = successBox;
        this.subFields = subFields;
    }

    public EndpointField(final String key, final String label, final String description, final boolean required, final String configureEndpoint, final Boolean successBox) {
        this(key, label, description, required, configureEndpoint, successBox, new ArrayList<>());
    }

    public EndpointField(final String key, final String label, final String description, final boolean required, final String configureEndpoint) {
        this(key, label, description, required, configureEndpoint, false, new ArrayList<>());
    }

    public EndpointField addSubField(final ConfigField field) {
        if (!(field instanceof EndpointField)) {
            subFields.add(field);
        }
        return this;
    }

    public String getConfigureEndpoint() {
        return configureEndpoint;
    }

    public Boolean getSuccessBox() {
        return successBox;
    }

    public List<ConfigField> getSubFields() {
        return subFields;
    }

}
