package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LabelValueSelectOptions extends AlertSerializableModel {
    private List<LabelValueSelectOption> options;

    public LabelValueSelectOptions() {
        this.options = List.of();
    }

    public LabelValueSelectOptions(List<LabelValueSelectOption> options) {
        this.options = options;
    }

    public List<LabelValueSelectOption> getOptions() {
        return options;
    }
}
