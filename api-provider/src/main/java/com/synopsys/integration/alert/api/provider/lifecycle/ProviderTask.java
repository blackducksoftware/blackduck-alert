/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider.lifecycle;

import java.util.List;

import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.api.provider.state.ProviderProperties;
import com.synopsys.integration.alert.api.task.ScheduledTask;
import com.synopsys.integration.alert.api.task.TaskMetaData;
import com.synopsys.integration.alert.api.task.TaskMetaDataProperty;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class ProviderTask extends ScheduledTask {
    private final ProviderProperties providerProperties;
    private final ProviderKey providerKey;
    private final String taskName;

    public ProviderTask(ProviderKey providerKey, TaskScheduler taskScheduler, ProviderProperties providerProperties) {
        super(taskScheduler);
        this.providerKey = providerKey;
        this.providerProperties = providerProperties;
        this.taskName = computeProviderTaskName(providerKey, getProviderProperties().getConfigId(), getClass());
    }

    @Override
    public final void runTask() {
        runProviderTask();
    }

    protected abstract void runProviderTask();

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskMetaData createTaskMetaData() {
        String fullyQualifiedName = ScheduledTask.computeFullyQualifiedName(getClass());
        String nextRunTime = getFormatedNextRunTime().orElse("");
        String providerName = providerKey.getDisplayName();
        String configName = providerProperties.getConfigName();
        TaskMetaDataProperty providerProperty = new TaskMetaDataProperty("provider", "Provider", providerName);
        TaskMetaDataProperty configurationProperty = new TaskMetaDataProperty("configurationName", "Configuration Name", configName);
        List<TaskMetaDataProperty> properties = List.of(providerProperty, configurationProperty);

        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), fullyQualifiedName, nextRunTime, properties);

    }

    protected ProviderProperties getProviderProperties() {
        return providerProperties;
    }

    private String computeProviderTaskName(ProviderKey providerKey, Long providerConfigId, Class<? extends ProviderTask> providerTaskClass) {
        String superTaskName = ScheduledTask.computeTaskName(providerTaskClass);
        String providerUniversalKey = providerKey.getUniversalKey();

        return String.format("%s::Provider[%s]::Configuration[id:%d]", superTaskName, providerUniversalKey, providerConfigId);
    }

}
