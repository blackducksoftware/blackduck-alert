/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling.descriptor;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public final class SchedulingDescriptorKey extends DescriptorKey {
    private static final String SCHEDULING_COMPONENT = "component_scheduling";

    public SchedulingDescriptorKey() {
        super(SCHEDULING_COMPONENT, SchedulingDescriptor.SCHEDULING_LABEL);
    }

}
