/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.scheduled.frequency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.ChannelTemplateManager;
import com.synopsys.integration.alert.common.digest.DigestNotificationProcessor;
import com.synopsys.integration.alert.common.enumeration.DigestType;
import com.synopsys.integration.alert.workflow.NotificationManager;

@Component
public class DailyTask extends ProcessingTask {
    public static final String TASK_NAME = "daily-frequency";

    @Autowired
    public DailyTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager, final DigestNotificationProcessor notificationProcessor, final ChannelTemplateManager channelTemplateManager) {
        super(taskScheduler, TASK_NAME, notificationManager, notificationProcessor, channelTemplateManager);
    }

    @Override
    public DigestType getDigestType() {
        return DigestType.DAILY;
    }
}
