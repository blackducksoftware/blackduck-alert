package com.blackduck.integration.alert.common.descriptor.action;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

public abstract class DescriptorAction {
    private final DescriptorKey descriptorKey;

    protected DescriptorAction(DescriptorKey descriptorKey) {
        this.descriptorKey = descriptorKey;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

}
