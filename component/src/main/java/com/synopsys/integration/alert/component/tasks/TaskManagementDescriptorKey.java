/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class TaskManagementDescriptorKey extends DescriptorKey {
    public static final String TASK_MANAGEMENT_COMPONENT = "component_tasks";

    public TaskManagementDescriptorKey() {
        super(TASK_MANAGEMENT_COMPONENT, TaskManagementDescriptor.TASK_MANAGEMENT_LABEL);
    }

}
