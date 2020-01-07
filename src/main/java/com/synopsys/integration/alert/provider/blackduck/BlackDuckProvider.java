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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.workflow.processor.ProviderMessageContentCollector;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckDistributionFilter;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckProjectSyncTask;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckProvider extends Provider {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckProvider.class);

    private final BlackDuckAccumulator accumulatorTask;
    private final BlackDuckProjectSyncTask projectSyncTask;
    private final TaskManager taskManager;
    private final BlackDuckProperties blackDuckProperties;

    private final ObjectFactory<BlackDuckDistributionFilter> distributionFilterFactory;
    private final ObjectFactory<BlackDuckMessageContentCollector> messageContentCollectorFactory;

    @Autowired
    public BlackDuckProvider(
        BlackDuckProviderKey blackDuckProviderKey, BlackDuckAccumulator accumulatorTask, BlackDuckProjectSyncTask projectSyncTask, BlackDuckContent blackDuckContent, TaskManager taskManager, BlackDuckProperties blackDuckProperties,
        ObjectFactory<BlackDuckDistributionFilter> distributionFilterFactory, ObjectFactory<BlackDuckMessageContentCollector> messageContentCollectorFactory) {
        super(blackDuckProviderKey, blackDuckContent);
        this.accumulatorTask = accumulatorTask;
        this.projectSyncTask = projectSyncTask;
        this.taskManager = taskManager;
        this.blackDuckProperties = blackDuckProperties;
        this.distributionFilterFactory = distributionFilterFactory;
        this.messageContentCollectorFactory = messageContentCollectorFactory;
    }

    @Override
    public void initialize() {
        logger.info("Initializing Black Duck provider...");
        taskManager.registerTask(accumulatorTask);
        taskManager.registerTask(projectSyncTask);

        final Optional<BlackDuckServerConfig> blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfigSafely(new Slf4jIntLogger(logger));
        blackDuckServerConfig.ifPresent(globalConfig -> {
            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, accumulatorTask.getTaskName());
            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, projectSyncTask.getTaskName());
        });
    }

    @Override
    public void destroy() {
        logger.info("Destroying Black Duck provider...");
        taskManager.unregisterTask(accumulatorTask.getTaskName());
        taskManager.unregisterTask(projectSyncTask.getTaskName());
    }

    @Override
    public ProviderDistributionFilter createDistributionFilter() {
        return distributionFilterFactory.getObject();
    }

    @Override
    public ProviderMessageContentCollector createMessageContentCollector() {
        return messageContentCollectorFactory.getObject();
    }

    @Override
    public ProviderNotificationClassMap getNotificationClassMap() {
        Map<String, Class<?>> notificationTypeToContentClass = new HashMap<>();
        notificationTypeToContentClass.put(NotificationType.BOM_EDIT.name(), BomEditNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.LICENSE_LIMIT.name(), LicenseLimitNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.POLICY_OVERRIDE.name(), PolicyOverrideNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.PROJECT.name(), ProjectNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.PROJECT_VERSION.name(), ProjectVersionNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.RULE_VIOLATION.name(), RuleViolationNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.RULE_VIOLATION_CLEARED.name(), RuleViolationClearedNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name(), VersionBomCodeLocationBomComputedNotificationView.class);
        notificationTypeToContentClass.put(NotificationType.VULNERABILITY.name(), VulnerabilityNotificationView.class);
        return new ProviderNotificationClassMap(notificationTypeToContentClass);
    }

}
