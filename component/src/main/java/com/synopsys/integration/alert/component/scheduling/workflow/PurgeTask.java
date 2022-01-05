/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.workflow;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.task.StartupScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.scheduling.SchedulingConfiguration;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;

@Component
public class PurgeTask extends StartupScheduledTask {
    public static final String CRON_FORMAT = "0 0 0 1/%s * ?";
    public static final int DEFAULT_FREQUENCY = 3;
    private static final int DEFAULT_DAY_OFFSET = 1;

    private final Logger logger = LoggerFactory.getLogger(PurgeTask.class);
    private final SchedulingDescriptorKey schedulingDescriptorKey;
    private final NotificationAccessor notificationAccessor;
    private final SystemMessageAccessor systemMessageAccessor;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private int dayOffset;

    @Autowired
    public PurgeTask(SchedulingDescriptorKey schedulingDescriptorKey, TaskScheduler taskScheduler, NotificationAccessor notificationAccessor, SystemMessageAccessor systemMessageAccessor, TaskManager taskManager,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        super(taskScheduler, taskManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.notificationAccessor = notificationAccessor;
        this.systemMessageAccessor = systemMessageAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.dayOffset = 1;
    }

    @Override
    public void runTask() {
        OffsetDateTime date = createNotificationOlderThanSearchDate();
        purgeNotifications(date);
        purgeSystemMessages(date);
    }

    @Override
    public String scheduleCronExpression() {
        String purgeSavedCronValue = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(schedulingDescriptorKey)
            .stream()
            .findFirst()
            .flatMap(configurationModel -> configurationModel.getField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS))
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(String.valueOf(DEFAULT_FREQUENCY));
        return String.format(CRON_FORMAT, purgeSavedCronValue);
    }

    @Override
    public void postTaskStartup() {
        CompletableFuture.supplyAsync(this::purgeOldData);
    }

    private void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }

    private void resetDayOffset() {
        setDayOffset(DEFAULT_DAY_OFFSET);
    }

    private void purgeNotifications(OffsetDateTime date) {
        try {
            logger.info("Purging notifications created earlier than {}...", date);
            int deletedCount = notificationAccessor.deleteNotificationsCreatedBefore(date);
            logger.info("Purged {} notifications", deletedCount);
        } catch (Exception ex) {
            logger.error("Error in purging notifications", ex);
        }
    }

    private void purgeSystemMessages(OffsetDateTime date) {
        try {
            int deletedCount = systemMessageAccessor.deleteSystemMessagesCreatedBefore(date);
            logger.debug("Purged {} system messages", deletedCount);
        } catch (Exception ex) {
            logger.error("Error purging system messages", ex);
        }
    }

    public OffsetDateTime createNotificationOlderThanSearchDate() {
        return DateUtils.createCurrentDateTimestamp()
            .minusDays(dayOffset)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private Boolean purgeOldData() {
        try {
            logger.info("Begin startup purge of old data");
            Optional<ConfigurationModel> configurationModel = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(schedulingDescriptorKey, ConfigContextEnum.GLOBAL).stream().findFirst();
            if (configurationModel.isPresent()) {
                Integer purgeDataFrequencyDays = configurationModel.map(SchedulingConfiguration::new)
                    .map(SchedulingConfiguration::getDataFrequencyDays)
                    .map(frequency -> NumberUtils.toInt(frequency, DEFAULT_FREQUENCY))
                    .orElse(DEFAULT_FREQUENCY);
                setDayOffset(purgeDataFrequencyDays);
                run();
                resetDayOffset();
                return Boolean.TRUE;
            }
        } catch (Exception ex) {
            logger.error("Error occurred purging data on startup", ex);
        } finally {
            logger.info("Finished startup purge of old data");
        }
        return Boolean.FALSE;
    }

}
