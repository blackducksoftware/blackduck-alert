/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
    private static final long serialVersionUID = -6213193510077419010L;
    private final String name;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private Set<AccessOperation> operations;
    private boolean readOnly;

    public DescriptorMetadata(DescriptorKey descriptorKey, DescriptorType type, ConfigContextEnum context) {
        this.name = descriptorKey.getUniversalKey();
        this.type = type;
        this.context = context;
        this.operations = Set.of();
    }

    public String getName() {
        return name;
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

    public void setOperations(Set<AccessOperation> operations) {
        this.operations = operations;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
