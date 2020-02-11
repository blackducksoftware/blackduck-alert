package com.synopsys.integration.alert.common.provider.lifecycle;

import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;

public abstract class ProviderTask extends ScheduledTask {
    private ProviderProperties providerProperties;

    public ProviderTask(TaskScheduler taskScheduler, String taskName) {
        super(taskScheduler, taskName);
        this.providerProperties = null;
    }

    public abstract void runProviderTask();

    @Override
    public final void runTask() {
        validateProviderProperties();
        runProviderTask();
        invalidateProviderProperties();
    }

    private void validateProviderProperties() {
        if (null == providerProperties) {
            throw new AlertRuntimeException("No provider properties were provided to the task.");
        }
    }

    private void invalidateProviderProperties() {
        if (null != providerProperties) {
            providerProperties.disconnect();
        }
        providerProperties = null;
    }

    public final void setProviderPropertiesForRun(ProviderProperties providerProperties) {
        this.providerProperties = providerProperties;
    }

    protected ProviderProperties getProviderProperties() {
        return providerProperties;
    }

}
