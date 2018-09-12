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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.ProjectSyncTask;

@Component(value = BlackDuckProvider.COMPONENT_NAME)
public class BlackDuckProvider extends Provider {
    public static final String COMPONENT_NAME = "provider_blackduck";
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckProvider.class);

    private final BlackDuckAccumulator accumulatorTask;
    private final ProjectSyncTask projectSyncTask;

    @Autowired
    public BlackDuckProvider(final BlackDuckAccumulator accumulatorTask, final ProjectSyncTask projectSyncTask) {
        this.accumulatorTask = accumulatorTask;
        this.projectSyncTask = projectSyncTask;
    }

    @Override
    public void initialize() {
        logger.info("Initializing provider...");
        accumulatorTask.scheduleExecution(BlackDuckAccumulator.DEFAULT_CRON_EXPRESSION);
        projectSyncTask.scheduleExecution(BlackDuckAccumulator.DEFAULT_CRON_EXPRESSION);
    }

    @Override
    public void destroy() {
        logger.info("Destroying provider...");
        accumulatorTask.scheduleExecution(BlackDuckAccumulator.STOP_SCHEDULE_EXPRESSION);
        projectSyncTask.scheduleExecution(BlackDuckAccumulator.STOP_SCHEDULE_EXPRESSION);
    }

    @Override
    public Set<ProviderContentType> getProviderContentTypes() {
        return BlackDuckProviderContentTypes.ALL.stream().collect(Collectors.toSet());
    }
}
