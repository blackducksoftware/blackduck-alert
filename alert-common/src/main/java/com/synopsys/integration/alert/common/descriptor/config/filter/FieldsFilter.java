package com.synopsys.integration.alert.common.descriptor.config.filter;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public abstract class FieldsFilter {
    private final String descriptorName;
    private final ConfigContextEnum context;

    public FieldsFilter(final String descriptorName, final ConfigContextEnum context) {
        this.descriptorName = descriptorName;
        this.context = context;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public ConfigContextEnum getContext() {
        return context;
    }

    public abstract List<ConfigField> filter(List<ConfigField> fields);
}
