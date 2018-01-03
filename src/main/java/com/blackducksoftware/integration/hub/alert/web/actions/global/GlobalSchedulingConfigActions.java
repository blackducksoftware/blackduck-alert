/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions.global;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSchedulingConfigRestModel;

@Component
public class GlobalSchedulingConfigActions extends ConfigActions<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel> {
    private final AccumulatorConfig accumulatorConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;
    private final PurgeConfig purgeConfig;

    @Autowired
    public GlobalSchedulingConfigActions(final AccumulatorConfig accumulatorConfig, final DailyDigestBatchConfig dailyDigestBatchConfig, final PurgeConfig purgeConfig, final GlobalSchedulingRepository repository,
            final ObjectTransformer objectTransformer) {
        super(GlobalSchedulingConfigEntity.class, GlobalSchedulingConfigRestModel.class, repository, objectTransformer);
        this.accumulatorConfig = accumulatorConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
        this.purgeConfig = purgeConfig;
    }

    @Override
    public List<String> sensitiveFields() {
        return Collections.emptyList();
    }

    @Override
    public String validateConfig(final GlobalSchedulingConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getAccumulatorCron())) {
            try {
                new CronTrigger(restModel.getAccumulatorCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("accumulatorCron", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(restModel.getDailyDigestCron())) {
            try {
                new CronTrigger(restModel.getDailyDigestCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("dailyDigestCron", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(restModel.getPurgeDataCron())) {
            try {
                new CronTrigger(restModel.getPurgeDataCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("purgeDataCron", e.getMessage());
            }
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String channelTestConfig(final GlobalSchedulingConfigRestModel restModel) throws IntegrationException {
        return "Not Implemented.";
    }

    @Override
    public void configurationChangeTriggers(final GlobalSchedulingConfigRestModel restModel) {
        if (restModel != null) {
            accumulatorConfig.scheduleJobExecution(restModel.getAccumulatorCron());
            dailyDigestBatchConfig.scheduleJobExecution(restModel.getDailyDigestCron());
            purgeConfig.scheduleJobExecution(restModel.getPurgeDataCron());
        }
    }

}
