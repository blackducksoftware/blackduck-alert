package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointField extends ConfigField {
    private final String buttonLabel;
    private final String endpoint;
    private final Boolean successBox;
    private final List<ConfigField> subFields;

    public static EndpointField create(final String key, final String label, final String description, final String buttonLabel, final String configureEndpoint) {
        return new EndpointField(key, label, description, false, buttonLabel, configureEndpoint);
    }

    public static EndpointField createRequired(final String key, final String label, final String description, final String buttonLabel, final String configureEndpoint) {
        return new EndpointField(key, label, description, true, configureEndpoint, buttonLabel);
    }

    public static EndpointField createWithSuccessBox(final String key, final String label, final String description, final String buttonLabel, final String configureEndpoint) {
        return new EndpointField(key, label, description, false, buttonLabel, configureEndpoint, true);
    }

    public static EndpointField createRequiredWithSuccessBox(final String key, final String label, final String description, final String buttonLabel, final String configureEndpoint) {
        return new EndpointField(key, label, description, true, buttonLabel, configureEndpoint, true);
    }

    private EndpointField(final String key, final String label, final String description, final boolean required, final String buttonLabel, final String endpoint, final Boolean successBox,
        final List<ConfigField> subFields) {
        super(key, label, description, FieldType.ENDPOINT.getFieldTypeName(), required, false);
        this.buttonLabel = buttonLabel;
        this.endpoint = endpoint;
        this.successBox = successBox;
        this.subFields = subFields;
    }

    public EndpointField(final String key, final String label, final String description, final boolean required, final String buttonLabel, final String endpoint, final Boolean successBox) {
        this(key, label, description, required, buttonLabel, endpoint, successBox, new ArrayList<>());
    }

    public EndpointField(final String key, final String label, final String description, final boolean required, final String buttonLabel, final String endpoint) {
        this(key, label, description, required, buttonLabel, endpoint, false, new ArrayList<>());
    }

    public EndpointField addSubField(final ConfigField field) {
        if (!(field instanceof EndpointField)) {
            subFields.add(field);
        }
        return this;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Boolean getSuccessBox() {
        return successBox;
    }

    public List<ConfigField> getSubFields() {
        return subFields;
    }

}
