/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Set;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
