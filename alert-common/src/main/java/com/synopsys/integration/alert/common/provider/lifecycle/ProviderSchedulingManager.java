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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;

@Component
public class ProviderSchedulingManager {
    private final Logger logger = LoggerFactory.getLogger(ProviderSchedulingManager.class);

    private List<Provider> providers;
    private TaskManager taskManager;
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    public ProviderSchedulingManager(List<Provider> providers, TaskManager taskManager, ConfigurationAccessor configurationAccessor) {
        this.providers = providers;
        this.taskManager = taskManager;
        this.configurationAccessor = configurationAccessor;
    }

    public List<ProviderTask> initializeConfiguredProviders() {
        List<ProviderTask> initializedTasks = new ArrayList<>();
        for (Provider provider : providers) {
            try {
                List<ConfigurationModel> providerConfigurations = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(provider.getKey(), ConfigContextEnum.GLOBAL);
                List<ProviderTask> initializedTasksForProvider = initializeConfiguredProviders(provider, providerConfigurations);
                initializedTasks.addAll(initializedTasksForProvider);
            } catch (AlertDatabaseConstraintException e) {
                logger.error("Could not retrieve provider config: ", e);
            }
        }
        return initializedTasks;
    }

    public List<ProviderTask> scheduleTasksForProviderConfig(Provider provider, ConfigurationModel providerConfig) throws AlertException {
        StatefulProvider statefulProvider = provider.createStatefulProvider(providerConfig);
        return scheduleTasksForProviderConfig(statefulProvider);
    }

    public List<ProviderTask> scheduleTasksForProviderConfig(StatefulProvider statefulProvider) throws AlertException {
        logger.debug("Performing scheduling tasks for config with id {} and provider {}", statefulProvider.getConfigId(), statefulProvider.getKey().getDisplayName());
        List<ProviderTask> acceptedTasks = new ArrayList<>();

        if (!statefulProvider.isConfigEnabled()) {
            throw new AlertException(String.format("The provider configuration '%s' cannot have tasks scheduled while it is disabled.", statefulProvider.getConfigName()));
        }

        List<ProviderTask> providerTasks = statefulProvider.getTasks();
        unscheduleTasksForProviderConfig(statefulProvider.getConfigId());
        for (ProviderTask task : providerTasks) {
            if (taskManager.getNextRunTime(task.getTaskName()).isEmpty()) {
                scheduleTask(task);
                acceptedTasks.add(task);
            }
        }
        logger.debug("Finished scheduling tasks for config with id {} and descriptor id {}", statefulProvider.getConfigId(), statefulProvider.getConfigId());
        return acceptedTasks;
    }

    public void unscheduleTasksForProviderConfig(Long providerConfigId) {
        logger.debug("Performing unscheduling tasks for provider config: id={}", providerConfigId);

        List<ProviderTask> tasks = taskManager.getTasksByClass(ProviderTask.class)
                                       .stream()
                                       .filter(task -> task.getProviderProperties().getConfigId().equals(providerConfigId))
                                       .collect(Collectors.toList());

        for (ProviderTask task : tasks) {
            unscheduleTask(task);
        }
        logger.debug("Finished unscheduling tasks for provider config: id={}", providerConfigId);
    }

    private List<ProviderTask> initializeConfiguredProviders(Provider provider, List<ConfigurationModel> providerConfigurations) {
        List<ProviderTask> initializedTasks = new ArrayList<>();
        for (ConfigurationModel providerConfig : providerConfigurations) {
            try {
                StatefulProvider statefulProvider = provider.createStatefulProvider(providerConfig);
                if (statefulProvider.isConfigEnabled()) {
                    List<ProviderTask> initializedTasksForConfig = scheduleTasksForProviderConfig(provider, providerConfig);
                    initializedTasks.addAll(initializedTasksForConfig);
                } else {
                    logger.debug("The provider configuration '{}' was disabled. No tasks will be scheduled for this config.", statefulProvider.getConfigName());
                }
            } catch (AlertException e) {
                logger.error("Something went wrong while attempting to schedule provider tasks", e);
            }
        }
        return initializedTasks;
    }

    private void scheduleTask(ProviderTask task) {
        taskManager.registerTask(task);
        taskManager.scheduleCronTask(task.scheduleCronExpression(), task.getTaskName());
    }

    private void unscheduleTask(ProviderTask task) {
        taskManager.unregisterTask(task.getTaskName());
    }

}
