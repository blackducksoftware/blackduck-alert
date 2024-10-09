package com.blackduck.integration.alert.component.scheduling.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public final class SchedulingDescriptorKey extends DescriptorKey {
    private static final String SCHEDULING_COMPONENT = "component_scheduling";

    public SchedulingDescriptorKey() {
        super(SCHEDULING_COMPONENT, SchedulingDescriptor.SCHEDULING_LABEL);
    }

}
