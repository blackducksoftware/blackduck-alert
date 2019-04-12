package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LabelValueSelectOption extends AlertSerializableModel {
    private String label;
    private String value;

    public LabelValueSelectOption(final String labelAndValue) {
        this.label = labelAndValue;
        this.value = labelAndValue;
    }

    public LabelValueSelectOption(final String label, final String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
