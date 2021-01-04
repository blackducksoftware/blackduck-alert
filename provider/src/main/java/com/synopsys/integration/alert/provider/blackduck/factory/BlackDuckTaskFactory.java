/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTaskFactory;
import com.synopsys.integration.alert.common.provider.state.ProviderProperties;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckDataSyncTask;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckValidator;

@Component
public class BlackDuckTaskFactory implements ProviderTaskFactory {
    private final BlackDuckProviderKey blackDuckProviderKey;
    private final TaskScheduler taskScheduler;
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final NotificationAccessor notificationAccessor;
    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private final BlackDuckValidator blackDuckValidator;
    private final EventManager eventManager;

    @Autowired
    public BlackDuckTaskFactory(
        BlackDuckProviderKey blackDuckProviderKey,
        TaskScheduler taskScheduler,
        ProviderDataAccessor blackDuckDataAccessor,
        NotificationAccessor notificationAccessor,
        ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor,
        BlackDuckValidator blackDuckValidator,
        EventManager eventManager
    ) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.taskScheduler = taskScheduler;
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.notificationAccessor = notificationAccessor;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
        this.blackDuckValidator = blackDuckValidator;
        this.eventManager = eventManager;
    }

    @Override
    public List<ProviderTask> createTasks(ProviderProperties providerProperties) {
        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(blackDuckProviderKey, taskScheduler, notificationAccessor, providerTaskPropertiesAccessor, providerProperties, blackDuckValidator, eventManager);
        BlackDuckDataSyncTask syncTask = new BlackDuckDataSyncTask(blackDuckProviderKey, taskScheduler, blackDuckDataAccessor, providerProperties);
        return List.of(accumulator, syncTask);
    }

}
