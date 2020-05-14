/**
 * blackduck-alert
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
package com.synopsys.integration.alert.workflow.scheduled;

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

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
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
    private final NotificationManager notificationManager;
    private final SystemMessageUtility systemMessageUtility;
    private final ConfigurationAccessor configurationAccessor;
    private int dayOffset;

    @Autowired
    public PurgeTask(SchedulingDescriptorKey schedulingDescriptorKey, TaskScheduler taskScheduler, NotificationManager notificationManager, SystemMessageUtility systemMessageUtility, TaskManager taskManager,
        ConfigurationAccessor configurationAccessor) {
        super(taskScheduler, taskManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.notificationManager = notificationManager;
        this.systemMessageUtility = systemMessageUtility;
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
        try {
            List<ConfigurationModel> schedulingConfigs = configurationAccessor.getConfigurationsByDescriptorKey(schedulingDescriptorKey);
            String purgeSavedCronValue = schedulingConfigs.stream()
                                             .findFirst()
                                             .flatMap(configurationModel -> configurationModel.getField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS))
                                             .flatMap(ConfigurationFieldModel::getFieldValue)
                                             .orElse(String.valueOf(DEFAULT_FREQUENCY));
            return String.format(CRON_FORMAT, purgeSavedCronValue);
        } catch (AlertDatabaseConstraintException e) {
            logger.error("Error connecting to DB", e);
        }

        return String.format(CRON_FORMAT, DEFAULT_FREQUENCY);
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
            OffsetDateTime date = createDate();
            logger.info("Searching for notifications to purge earlier than {}", date);
            List<AlertNotificationModel> notifications = notificationManager.findByCreatedAtBefore(date);

            if (notifications == null || notifications.isEmpty()) {
                logger.info("No notifications found to purge");
            } else {
                logger.info("Found {} notifications to purge", notifications.size());
                logger.info("Purging {} notifications.", notifications.size());
                notificationManager.deleteNotificationList(notifications);
            }
        } catch (Exception ex) {
            logger.error("Error in purging notifications", ex);
        }
    }

    private void purgeSystemMessages() {
        try {
            OffsetDateTime date = createDate();
            List<SystemMessageModel> messages = systemMessageUtility.getSystemMessagesBefore(date);
            systemMessageUtility.deleteSystemMessages(messages);
        } catch (Exception ex) {
            logger.error("Error purging system messages", ex);
        }
    }

    // TODO give this method a more descriptive name
    public OffsetDateTime createDate() {
        return OffsetDateTime.now()
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
