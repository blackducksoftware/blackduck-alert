/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
