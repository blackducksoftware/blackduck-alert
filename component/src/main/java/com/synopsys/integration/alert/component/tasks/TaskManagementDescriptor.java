/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.tasks;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

@Component
public class TaskManagementDescriptor extends ComponentDescriptor {
    public static final String TASK_MANAGEMENT_LABEL = "Task Management";
    public static final String TASK_MANAGEMENT_URL = "tasks";
    public static final String TASK_MANAGEMENT_DESCRIPTION = "This page allows you to view the tasks running internally within Alert.";
    public static final String TASKS_COMPONENT_NAMESPACE = "tasks.TaskManagement";

    @Autowired
    public TaskManagementDescriptor(TaskManagementDescriptorKey descriptorKey) {
        super(descriptorKey);
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.empty();
    }

}
