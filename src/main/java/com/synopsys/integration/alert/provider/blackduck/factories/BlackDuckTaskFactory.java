package com.synopsys.integration.alert.provider.blackduck.factories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.provider.ProviderProperties;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTaskFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckDataSyncTask;

@Component
public class BlackDuckTaskFactory implements ProviderTaskFactory {
    private final BlackDuckProviderKey blackDuckProviderKey;
    private final TaskScheduler taskScheduler;
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final NotificationManager notificationManager;
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public BlackDuckTaskFactory(BlackDuckProviderKey blackDuckProviderKey, TaskScheduler taskScheduler, ProviderDataAccessor blackDuckDataAccessor,
        ConfigurationAccessor configurationAccessor, NotificationManager notificationManager, FilePersistenceUtil filePersistenceUtil) {
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.taskScheduler = taskScheduler;
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.configurationAccessor = configurationAccessor;
        this.notificationManager = notificationManager;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    @Override
    public List<ProviderTask> createTasks(ProviderProperties providerProperties) {
        BlackDuckAccumulator accumulator = new BlackDuckAccumulator(blackDuckProviderKey, taskScheduler, notificationManager, filePersistenceUtil);
        accumulator.setProviderPropertiesForRun(providerProperties);
        BlackDuckDataSyncTask syncTask = new BlackDuckDataSyncTask(blackDuckProviderKey, taskScheduler, blackDuckDataAccessor, configurationAccessor);
        return List.of(accumulator, syncTask);
    }
}
