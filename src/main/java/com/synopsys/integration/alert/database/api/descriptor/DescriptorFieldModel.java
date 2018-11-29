package com.synopsys.integration.alert.database.api.descriptor;

import com.synopsys.integration.util.Stringable;

public class DescriptorFieldModel extends Stringable {
    private final String key;
    private final Boolean sensitive;

    public DescriptorFieldModel(final String key, final Boolean sensitive) {
        this.key = key;
        this.sensitive = sensitive;
    }

    public String getKey() {
        return key;
    }

    public Boolean getSensitive() {
        return sensitive;
    }
}
