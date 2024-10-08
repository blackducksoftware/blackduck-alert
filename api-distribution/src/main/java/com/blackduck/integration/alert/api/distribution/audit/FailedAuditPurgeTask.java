package com.blackduck.integration.alert.api.distribution.audit;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.task.StartupScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
public class FailedAuditPurgeTask extends StartupScheduledTask {
    public static final String CRON_EXPRESSION_FORMAT = "0 0 0 1/%s * ?";
    public static final Integer DEFAULT_FREQUENCY = 10;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProcessingFailedAccessor failedAuditAccessor;

    //TODO Remove these when the scheduling configuration has been changed from a FieldModel design.
    public static final String KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS = "scheduling.purge.data.audit.failed.frequency";
    public static final String DESCRIPTOR_NAME = "component_scheduling";
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public FailedAuditPurgeTask(
        TaskScheduler taskScheduler,
        TaskManager taskManager,
        ProcessingFailedAccessor failedAuditAccessor,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor
    ) {
        super(taskScheduler, taskManager);
        this.failedAuditAccessor = failedAuditAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public String scheduleCronExpression() {
        Integer purgeSavedCronValue = getConfiguredFrequency();
        return String.format(CRON_EXPRESSION_FORMAT, purgeSavedCronValue);
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
            .minusDays(getConfiguredFrequency())
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        logger.info("Purging old failed Audit entries older than {}", purgeDate);
        failedAuditAccessor.deleteAuditEntriesBefore(purgeDate);
        return Boolean.TRUE;
    }

    private Integer getConfiguredFrequency() {
        return configurationModelConfigurationAccessor.getConfigurationsByDescriptorNameAndContext(
                DESCRIPTOR_NAME,
                ConfigContextEnum.GLOBAL
            ).stream()
            .map(ConfigurationModel::getCopyOfKeyToFieldMap)
            .map(FieldUtility::new)
            .map(fieldUtility -> fieldUtility.getInteger(KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS))
            .flatMap(Optional::stream)
            .findFirst()
            .orElse(DEFAULT_FREQUENCY);
    }
}
