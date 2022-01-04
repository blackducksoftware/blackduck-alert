/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.api.provider.lifecycle.ProviderTaskFactory;
import com.synopsys.integration.alert.api.provider.state.ProviderProperties;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderTaskPropertiesAccessor;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckDataSyncTask;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckNotificationRetrieverFactory;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;

@Component
public class BlackDuckTaskFactory implements ProviderTaskFactory {
    private final BlackDuckProviderKey blackDuckProviderKey;
    private final TaskScheduler taskScheduler;
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final NotificationAccessor notificationAccessor;
    private final ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor;
    private final BlackDuckSystemValidator blackDuckSystemValidator;
    private final EventManager eventManager;
    private final BlackDuckNotificationRetrieverFactory notificationRetrieverFactory;

    @Autowired
    public BlackDuckTaskFactory(
        BlackDuckProviderKey blackDuckProviderKey,
        TaskScheduler taskScheduler,
        ProviderDataAccessor blackDuckDataAccessor,
        NotificationAccessor notificationAccessor,
        ProviderTaskPropertiesAccessor providerTaskPropertiesAccessor,
        BlackDuckSystemValidator blackDuckSystemValidator,
        EventManager eventManager,
        BlackDuckNotificationRetrieverFactory notificationRetrieverFactory
    ) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.taskScheduler = taskScheduler;
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.notificationAccessor = notificationAccessor;
        this.providerTaskPropertiesAccessor = providerTaskPropertiesAccessor;
        this.blackDuckSystemValidator = blackDuckSystemValidator;
        this.eventManager = eventManager;
        this.notificationRetrieverFactory = notificationRetrieverFactory;
    }

    @Override
    public List<ProviderTask> createTasks(ProviderProperties providerProperties) {
        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(
            blackDuckProviderKey,
            taskScheduler,
            notificationAccessor,
            providerTaskPropertiesAccessor,
            providerProperties,
            blackDuckSystemValidator,
            eventManager,
            notificationRetrieverFactory
        );
        BlackDuckDataSyncTask syncTask = new BlackDuckDataSyncTask(blackDuckProviderKey, taskScheduler, blackDuckDataAccessor, providerProperties);
        return List.of(accumulator, syncTask);
    }

}
