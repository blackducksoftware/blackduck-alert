/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class ComponentDescriptor extends Descriptor {
    public ComponentDescriptor(DescriptorKey descriptorKey, UIConfig componentUIConfig) {
        this(descriptorKey, componentUIConfig, null);
    }

    public ComponentDescriptor(DescriptorKey descriptorKey, UIConfig componentUIConfig, GlobalValidator globalValidator) {
        super(descriptorKey, DescriptorType.COMPONENT, globalValidator);
        addGlobalUiConfig(componentUIConfig);
    }
}
