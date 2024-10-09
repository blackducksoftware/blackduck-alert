package com.blackduck.integration.alert.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.task.StartupScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.common.enumeration.SystemMessageSeverity;
import com.blackduck.integration.alert.common.enumeration.SystemMessageType;
import com.blackduck.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.blackduck.integration.alert.update.model.UpdateModel;

@Component
public class UpdateNotifierTask extends StartupScheduledTask {
    public static final String TASK_NAME = "updatenotifier";
    public static final String CRON_EXPRESSION = "0 0 12 1/1 * ?";

    private final UpdateChecker updateChecker;
    private final SystemMessageAccessor systemMessageAccessor;
    private final UpdateEmailService updateEmailService;

    @Autowired
    public UpdateNotifierTask(TaskScheduler taskScheduler, UpdateChecker updateChecker, SystemMessageAccessor systemMessageAccessor, UpdateEmailService updateEmailService, TaskManager taskManager) {
        super(taskScheduler, taskManager);
        this.updateChecker = updateChecker;
        this.systemMessageAccessor = systemMessageAccessor;
        this.updateEmailService = updateEmailService;
    }

    @Override
    public void runTask() {
        UpdateModel updateModel = updateChecker.getUpdateModel();
        if (updateModel.getUpdatable()) {
            addSystemMessage(updateModel.getDockerTagVersion());
            updateEmailService.sendUpdateEmail(updateModel);
        }
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    private void addSystemMessage(String versionName) {
        String message = String.format("There is a new version of %s available: %s", AlertConstants.ALERT_APPLICATION_NAME, versionName);
        systemMessageAccessor.removeSystemMessagesByType(SystemMessageType.UPDATE_AVAILABLE);
        systemMessageAccessor.addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.UPDATE_AVAILABLE);
    }

}
