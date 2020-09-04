package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ProviderProjectSelectOption extends AlertSerializableModel {
    private String name;
    private String description;

    public ProviderProjectSelectOption(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
