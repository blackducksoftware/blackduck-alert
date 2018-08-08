package com.blackducksoftware.integration.alert.common.descriptor.config;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.alert.common.enumeration.FieldGroup;
import com.blackducksoftware.integration.alert.common.enumeration.FieldType;

public class ConfigField {
    private String key;
    private String label;
    private FieldType type;
    private boolean required;
    private FieldGroup group;
    private List<String> options;

    public ConfigField(final String key, final String label, final FieldType type, final boolean required, final FieldGroup group, final List<String> options) {
        super();
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.group = group;
        this.options = options;
    }

    public ConfigField(final String key, final String label, final FieldType type, final boolean required, final FieldGroup group) {
        this(key, label, type, required, group, Arrays.asList());
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(final FieldType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public FieldGroup getGroup() {
        return group;
    }

    public void setGroup(final FieldGroup group) {
        this.group = group;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }

}
