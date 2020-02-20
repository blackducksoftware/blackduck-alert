/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.provider.lifecycle;

import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;

public abstract class ProviderTask extends ScheduledTask {
    private ProviderKey providerKey;
    private ProviderProperties providerProperties;

    // TODO reevaluate if this is needed once scheduling supports multiple providers
    public static String computeProviderTaskName(ProviderKey providerKey, Long providerConfigId, Class<? extends ProviderTask> providerTaskClass) {
        String superTaskName = ScheduledTask.computeTaskName(providerTaskClass);
        String providerUniversalKey = providerKey.getUniversalKey();

        return String.format("%s::Provider[%s]::Configuration[id:%d]", superTaskName, providerUniversalKey, providerConfigId);
    }

    public ProviderTask(ProviderKey providerKey, TaskScheduler taskScheduler, ProviderProperties providerProperties) {
        super(taskScheduler);
        this.providerKey = providerKey;
        this.providerProperties = providerProperties;
    }

    @Override
    public final void runTask() {
        validateProviderProperties();
        runProviderTask();
        invalidateProviderProperties();
    }

    public abstract void runProviderTask();

    @Override
    public String getTaskName() {
        validateProviderProperties();
        return ProviderTask.computeProviderTaskName(providerKey, getProviderProperties().getConfigId(), getClass());
    }

    protected ProviderProperties getProviderProperties() {
        return providerProperties;
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

}
