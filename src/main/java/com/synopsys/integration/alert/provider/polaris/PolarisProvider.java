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
package com.synopsys.integration.alert.provider.polaris;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisContent;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

//@Component(PolarisProvider.COMPONENT_NAME)
public class PolarisProvider extends Provider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TaskManager taskManager;
    private final PolarisProjectSyncTask projectSyncTask;
    private final PolarisProperties polarisProperties;

    @Autowired
    public PolarisProvider(PolarisProviderKey polarisProviderKey, TaskManager taskManager, PolarisProjectSyncTask projectSyncTask, PolarisProperties polarisProperties, PolarisContent polarisContent) {
        super(polarisProviderKey, polarisContent);
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
    public ProviderDistributionFilter createDistributionFilter() {
        return new ProviderDistributionFilter() {
            @Override
            public boolean doesNotificationApplyToConfiguration(AlertNotificationWrapper notification, ConfigurationJobModel configurationJobModel) {
                return false;
            }

            @Override
            public NotificationDeserializationCache getCache() {
                return null;
            }
        };
    }

    @Override
    public ProviderMessageContentCollector createMessageContentCollector() {
        return new ProviderMessageContentCollector(List.of()) {
            @Override
            protected List<ProviderMessageContent> createProviderMessageContents(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationWrapper> notifications) throws AlertException {
                return List.of();
            }
        };
    }

    @Override
    public ProviderNotificationClassMap getNotificationClassMap() {
        // add legitimate class mappings if needed
        return new ProviderNotificationClassMap(Map.of());
    }

}
