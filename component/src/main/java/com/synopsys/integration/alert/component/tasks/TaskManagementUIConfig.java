/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.CustomUIConfig;

@Component
public class TaskManagementUIConfig extends CustomUIConfig {
    public TaskManagementUIConfig() {
        super(TaskManagementDescriptor.TASK_MANAGEMENT_LABEL, TaskManagementDescriptor.TASK_MANAGEMENT_DESCRIPTION, TaskManagementDescriptor.TASK_MANAGEMENT_URL, TaskManagementDescriptor.TASKS_COMPONENT_NAMESPACE);
    }
}
