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
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;

@Component
public class OAuthRequestPurgeTask extends StartupScheduledTask {
    private static final String CRON_EXPRESSION = "0 0/5 * 1/1 * ?";
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public OAuthRequestPurgeTask(TaskScheduler taskScheduler, TaskManager taskManager, OAuthRequestValidator oAuthRequestValidator) {
        super(taskScheduler, taskManager);
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    @Override
    public String scheduleCronExpression() {
        return CRON_EXPRESSION;
    }

    @Override
    public void runTask() {
        Instant requestsBefore = Instant.now().minus(5, ChronoUnit.MINUTES);
        oAuthRequestValidator.removeRequestsOlderThanInstant(requestsBefore);
    }
}
