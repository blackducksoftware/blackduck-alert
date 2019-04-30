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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckProjectSyncTask;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component(value = BlackDuckProvider.COMPONENT_NAME)
public class BlackDuckProvider extends Provider {
    public static final String COMPONENT_NAME = "provider_blackduck";
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckProvider.class);

    private final BlackDuckAccumulator accumulatorTask;
    private final BlackDuckProjectSyncTask projectSyncTask;
    private final TaskManager taskManager;
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckProvider(final BlackDuckAccumulator accumulatorTask, final BlackDuckProjectSyncTask projectSyncTask, final DefaultEmailHandler blackDuckEmailHandler, final TaskManager taskManager,
        final BlackDuckProperties blackDuckProperties) {
        super(BlackDuckProvider.COMPONENT_NAME, blackDuckEmailHandler);
        this.accumulatorTask = accumulatorTask;
        this.projectSyncTask = projectSyncTask;
        this.taskManager = taskManager;
        this.blackDuckProperties = blackDuckProperties;
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
    public Set<ProviderContentType> getProviderContentTypes() {
        final Predicate<ProviderContentType> excludeBom = Predicate.isEqual(BlackDuckProviderContentTypes.BOM_EDIT);
        final Predicate<ProviderContentType> filterExcludedTypes = excludeBom.negate();
        return BlackDuckProviderContentTypes.ALL.stream().filter(filterExcludedTypes).sorted(Comparator.comparing(ProviderContentType::getNotificationType)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.of(FormatType.DEFAULT, FormatType.DIGEST, FormatType.SUMMARY);
    }

}
