package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model;

import java.util.List;

public class ProviderProjectOptions {
    private List<ProviderProjectSelectOption> options;

    public ProviderProjectOptions(List<ProviderProjectSelectOption> options) {
        this.options = options;
    }

    public List<ProviderProjectSelectOption> getOptions() {
        return options;
    }
}
