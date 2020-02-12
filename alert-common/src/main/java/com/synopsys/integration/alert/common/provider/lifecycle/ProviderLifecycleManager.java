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

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;

@Component
public class ProviderLifecycleManager {
    private List<Provider> providers;
    private TaskManager taskManager;
    private ConfigurationAccessor configurationAccessor;
    private DescriptorAccessor descriptorAccessor;

    public void initializeTasksForValidProviders() {
        // TODO implement
    }

    // TODO consider a callback for scheduled task completion
    public void runTasksForProviderConfig(ConfigurationModel providerConfig) {
        try {
            RegisteredDescriptorModel providerDescriptor = descriptorAccessor.getRegisteredDescriptorById(providerConfig.getDescriptorId())
                                                               .orElseThrow(() -> new AlertException("The provider did not exist"));
            providerDescriptor.getName();

            //ProviderProperties providerProperties = provider.createProperties(providerConfig);
            //List<ProviderTask> providerTasks = provider.createProviderTasks(providerProperties);
            //runTasks(providerTasks, providerConfig);
            //        logger.info("Scheduling tasks for <PROVIDER_NAME> provider...");
        } catch (AlertException e) {
            // TODO handle
        }

        //        taskManager.registerTask(accumulatorTask);
        //        taskManager.registerTask(projectSyncTask);
        //
        //        Optional<BlackDuckServerConfig> blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfigSafely(new Slf4jIntLogger(logger));
        //        blackDuckServerConfig.ifPresent(globalConfig -> {
        //            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, accumulatorTask.getTaskName());
        //            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, projectSyncTask.getTaskName());
        //        });
    }

    private void runTasks(List<ProviderTask> tasks, ConfigurationModel providerConfig) {
        // TODO use the provider's property factory to create the properties for this provider config
        for (ProviderTask providerTask : tasks) {
            // TODO inject the properties: providerTask.setProviderPropertiesForRun();

            // TODO schedule the task
        }
    }

    // DESTROY?
    //        logger.info("Destroying <PROVIDER_NAME> provider...");
    //        taskManager.unregisterTask(accumulatorTask.getTaskName());
    //        taskManager.unregisterTask(projectSyncTask.getTaskName());

}
