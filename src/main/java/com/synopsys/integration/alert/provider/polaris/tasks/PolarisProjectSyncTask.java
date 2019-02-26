/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.polaris.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final AccessTokenPolarisHttpClient polarisHttpClient = polarisProperties.createPolarisHttpClient(logger);
            // TODO get projects and store them, tracking deltas
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
        logger.info("### Finished {}...", getTaskName());
    }
}
