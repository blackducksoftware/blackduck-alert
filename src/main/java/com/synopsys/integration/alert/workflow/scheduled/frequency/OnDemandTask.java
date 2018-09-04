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
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;

@Component
public class OnDemandTask extends ProcessingTask {
    public static final long DEFAULT_INTERVAL_MILLISECONDS = 10000;
    public static final String TASK_NAME = "ondemand-frequency";

    @Autowired
    public OnDemandTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager,
    final NotificationProcessor notificationProcessor, final ChannelTemplateManager channelTemplateManager) {
        super(taskScheduler, TASK_NAME, notificationManager, notificationProcessor, channelTemplateManager);
    }

    @Override
    public FrequencyType getDigestType() {
        return FrequencyType.REAL_TIME;
    }
}
