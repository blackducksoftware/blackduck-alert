package com.synopsys.integration.alert.provider.polaris;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.event.ConfigurationEvent;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component
public class PolarisEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final TaskManager taskManager;

    @Autowired
    public PolarisEventListener(final PolarisProperties polarisProperties, final TaskManager taskManager) {
        this.polarisProperties = polarisProperties;
        this.taskManager = taskManager;
    }

    @EventListener(condition = "#configurationEvent.configurationName == 'provider_polaris' && (#configurationEvent.eventType.name() == 'CONFIG_UPDATE_AFTER' || #configurationEvent.eventType.name() == 'CONFIG_SAVE_AFTER')")
    public void handleNewOrUpdatedConfig(final ConfigurationEvent configurationEvent) {
        final Optional<AccessTokenPolarisHttpClient> polarisHttpClient = polarisProperties.createPolarisHttpClientSafely(logger);
        final Optional<String> nextRunTime = taskManager.getNextRunTime(PolarisProjectSyncTask.TASK_NAME);
        if (polarisHttpClient.isPresent() && nextRunTime.isEmpty()) {
            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, PolarisProjectSyncTask.TASK_NAME);
        }
    }

    @EventListener(condition = "#configurationEvent.configurationName == 'provider_polaris' && #configurationEvent.eventType.name() == 'CONFIG_DELETE_AFTER'")
    public void handleDeleteConfig(final ConfigurationEvent configurationEvent) {
        taskManager.unScheduleTask(PolarisProjectSyncTask.TASK_NAME);
    }
}
