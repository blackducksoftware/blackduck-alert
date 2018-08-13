package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class DropDownConfigField extends ConfigField {
    private List<String> options;

    public DropDownConfigField(final String key, final String label, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup, final List<String> options) {
        super(key, label, FieldType.SELECT.getFieldTypeName(), required, sensitive, group, subGroup);
        this.options = options;
    }

    public DropDownConfigField(final String key, final String label, final boolean required, final boolean sensitive, final List<String> options) {
        this(key, label, required, sensitive, FieldGroup.DEFAULT, "", options);
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }
}
