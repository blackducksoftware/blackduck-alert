package com.blackduck.integration.alert.common.persistence.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class PermissionKey extends AlertSerializableModel {
    private final String context;
    private final String descriptorName;

    public PermissionKey(String context, String descriptorName) {
        this.context = context;
        this.descriptorName = descriptorName;
    }

    public String getContext() {
        return context;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

}
