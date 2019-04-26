/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.polaris;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.DefaultEmailHandler;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component(PolarisProvider.COMPONENT_NAME)
public class PolarisProvider extends Provider {
    public static final String COMPONENT_NAME = "provider_polaris";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskManager taskManager;
    private final PolarisProjectSyncTask projectSyncTask;
    private final PolarisProperties polarisProperties;

    @Autowired
    public PolarisProvider(final TaskManager taskManager, final PolarisProjectSyncTask projectSyncTask, final PolarisProperties polarisProperties, final DefaultEmailHandler defaultEmailHandler) {
        super(PolarisProvider.COMPONENT_NAME, defaultEmailHandler);
        this.taskManager = taskManager;
        this.projectSyncTask = projectSyncTask;
        this.polarisProperties = polarisProperties;
    }

    @Override
    public void initialize() {
        logger.info("Initializing Polaris provider...");
        taskManager.registerTask(projectSyncTask);
        final Optional<AccessTokenPolarisHttpClient> polarisHttpClient = polarisProperties.createPolarisHttpClientSafely(logger);
        polarisHttpClient.ifPresent(client -> {
            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, projectSyncTask.getTaskName());
        });
    }

    @Override
    public void destroy() {
        logger.info("Destroying Polaris provider...");
        taskManager.unregisterTask(projectSyncTask.getTaskName());
    }

    @Override
    public Set<ProviderContentType> getProviderContentTypes() {
        return Set.of(PolarisProviderContentTypes.ISSUE_COUNT_INCREASED, PolarisProviderContentTypes.ISSUE_COUNT_DECREASED);
    }

    @Override
    public Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.of(FormatType.DEFAULT, FormatType.SUMMARY);
    }

}
