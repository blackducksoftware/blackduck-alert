package com.synopsys.integration.alert.api.distribution.audit;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.task.StartupScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class FailedAuditPurgeTask extends StartupScheduledTask {
    public static final String CRON_EXPRESSION = "0 0 0 1/10 * ?";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProcessingFailedAccessor failedAuditAccessor;

    @Autowired
    public FailedAuditPurgeTask(TaskScheduler taskScheduler, TaskManager taskManager, ProcessingFailedAccessor failedAuditAccessor) {
        super(taskScheduler, taskManager);
        this.failedAuditAccessor = failedAuditAccessor;
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    @Override
    public void runTask() {
        purgeOldData();
    }

    @Override
    public void postTaskStartup() {
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    private Boolean purgeOldData() {
        OffsetDateTime purgeDate = DateUtils.createCurrentDateTimestamp()
            .minusDays(10)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        logger.info("Purging old failed Audit entries older than {}", purgeDate);
        failedAuditAccessor.deleteAuditEntriesBefore(purgeDate);
        return Boolean.TRUE;
    }
}
