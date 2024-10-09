package com.blackduck.integration.alert.component.tasks;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public class TaskManagementDescriptorKey extends DescriptorKey {
    public static final String TASK_MANAGEMENT_COMPONENT = "component_tasks";

    public TaskManagementDescriptorKey() {
        super(TASK_MANAGEMENT_COMPONENT, TaskManagementDescriptor.TASK_MANAGEMENT_LABEL);
    }

}
