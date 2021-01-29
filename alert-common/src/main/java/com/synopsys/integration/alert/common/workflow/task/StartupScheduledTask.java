/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

public abstract class StartupScheduledTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(StartupScheduledTask.class);

    private final TaskManager taskManager;
    private boolean enabled;

    public StartupScheduledTask(TaskScheduler taskScheduler, TaskManager taskManager) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.enabled = true;
    }

    public void checkTaskEnabled() {
        enabled = true;
    }

    public void startTask() {
        checkTaskEnabled();
        String taskName = getTaskName();
        if (!getEnabled()) {
            logger.info("{} is disabled and will not be scheduled to run.", taskName);
            return;
        }
        taskManager.registerTask(this);
        taskManager.scheduleCronTask(scheduleCronExpression(), taskName);
        String nextRun = taskManager.getNextRunTime(taskName).orElse("");
        logger.info("{} next run:     {}", taskName, nextRun);
        postTaskStartup();
    }

    protected void postTaskStartup() {

    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
