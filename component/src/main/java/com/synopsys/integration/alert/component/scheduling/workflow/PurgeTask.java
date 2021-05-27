/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.workflow;

import java.time.OffsetDateTime;
import java.util.List;
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
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
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
    private final ConfigurationAccessor configurationAccessor;
    private int dayOffset;

    @Autowired
    public PurgeTask(SchedulingDescriptorKey schedulingDescriptorKey, TaskScheduler taskScheduler, NotificationAccessor notificationAccessor, SystemMessageAccessor systemMessageAccessor, TaskManager taskManager,
        ConfigurationAccessor configurationAccessor) {
        super(taskScheduler, taskManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.notificationAccessor = notificationAccessor;
        this.systemMessageAccessor = systemMessageAccessor;
        this.configurationAccessor = configurationAccessor;
        this.dayOffset = 1;
    }

    @Override
    public void runTask() {
        purgeNotifications();
        purgeSystemMessages();
    }

    @Override
    public String scheduleCronExpression() {
        String purgeSavedCronValue = configurationAccessor.getConfigurationsByDescriptorKey(schedulingDescriptorKey)
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

    public void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }

    public void resetDayOffset() {
        setDayOffset(DEFAULT_DAY_OFFSET);
    }

    private void purgeNotifications() {
        try {
            OffsetDateTime date = createNotificationOlderThanSearchDate();
            logger.info("Searching for notifications to purge earlier than {}", date);
            List<AlertNotificationModel> notifications = notificationAccessor.findByCreatedAtBefore(date);

            if (notifications == null || notifications.isEmpty()) {
                logger.info("No notifications found to purge");
            } else {
                logger.info("Found {} notifications to purge", notifications.size());
                logger.info("Purging {} notifications.", notifications.size());
                notificationAccessor.deleteNotificationList(notifications);
            }
        } catch (Exception ex) {
            logger.error("Error in purging notifications", ex);
        }
    }

    private void purgeSystemMessages() {
        try {
            OffsetDateTime date = createNotificationOlderThanSearchDate();
            List<SystemMessageModel> messages = systemMessageAccessor.getSystemMessagesBefore(date);
            systemMessageAccessor.deleteSystemMessages(messages);
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
            Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(schedulingDescriptorKey, ConfigContextEnum.GLOBAL).stream().findFirst();
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
