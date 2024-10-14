/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.config.ui;

import java.util.Set;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.AccessOperation;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;

public class DescriptorMetadata extends AlertSerializableModel {
    private final String name;
    private final String label;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private final Set<AccessOperation> operations;
    private final boolean readOnly;

    public DescriptorMetadata(DescriptorKey descriptorKey, DescriptorType type, ConfigContextEnum context, Set<AccessOperation> operations, boolean readOnly) {
        this.name = descriptorKey.getUniversalKey();
        this.label = descriptorKey.getDisplayName();
        this.type = type;
        this.context = context;
        this.operations = operations;
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public DescriptorType getType() {
        return type;
    }

    public ConfigContextEnum getContext() {
        return context;
    }

    public Set<AccessOperation> getOperations() {
        return operations;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

}
