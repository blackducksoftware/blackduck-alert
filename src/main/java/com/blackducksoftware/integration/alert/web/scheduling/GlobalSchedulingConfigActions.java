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
package com.blackducksoftware.integration.alert.web.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.alert.config.PurgeConfig;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingRepository;
import com.blackducksoftware.integration.alert.provider.hub.accumulator.NotificationAccumulator;
import com.blackducksoftware.integration.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class GlobalSchedulingConfigActions extends ConfigActions<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {
    private final NotificationAccumulator blackDuckAccumulator;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;
    private final PurgeConfig purgeConfig;

    @Autowired
    public GlobalSchedulingConfigActions(final NotificationAccumulator blackDuckAccumulator, final DailyDigestBatchConfig dailyDigestBatchConfig, final PurgeConfig purgeConfig, final GlobalSchedulingRepository repository,
            final GlobalSchedulingContentConverter contentConverter) {
        super(repository, contentConverter);
        this.blackDuckAccumulator = blackDuckAccumulator;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
        this.purgeConfig = purgeConfig;
    }

    @Override
    public List<GlobalSchedulingConfigRestModel> getConfig(final Long id) throws AlertException {
        GlobalSchedulingConfigEntity databaseEntity = null;
        if (id != null) {
            final Optional<GlobalSchedulingConfigEntity> repositoryResult = getRepository().findById(id);
            if (repositoryResult.isPresent()) {
                databaseEntity = repositoryResult.get();
            }
        } else {
            final List<GlobalSchedulingConfigEntity> databaseEntities = getRepository().findAll();
            if (databaseEntities != null && !databaseEntities.isEmpty()) {
                databaseEntity = databaseEntities.get(0);
            }
        }
        final GlobalSchedulingConfigRestModel restModel;
        if (databaseEntity != null) {
            restModel = (GlobalSchedulingConfigRestModel) getDatabaseContentConverter().populateRestModelFromDatabaseEntity(databaseEntity);
            restModel.setDailyDigestNextRun(dailyDigestBatchConfig.getFormatedNextRunTime());
            restModel.setPurgeDataNextRun(purgeConfig.getFormatedNextRunTime());
        } else {
            restModel = new GlobalSchedulingConfigRestModel();
        }
        final Long accumulatorNextRun = blackDuckAccumulator.getMillisecondsToNextRun();
        if (accumulatorNextRun != null) {
            final Long seconds = TimeUnit.MILLISECONDS.toSeconds(blackDuckAccumulator.getMillisecondsToNextRun());
            restModel.setAccumulatorNextRun(String.valueOf(seconds));
        }
        final List<GlobalSchedulingConfigRestModel> restModels = new ArrayList<>();
        restModels.add(restModel);
        return restModels;
    }

    @Override
    public String validateConfig(final GlobalSchedulingConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getDailyDigestHourOfDay())) {
            if (!StringUtils.isNumeric(restModel.getDailyDigestHourOfDay())) {
                fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
            } else {
                final Integer integer = Integer.valueOf(restModel.getDailyDigestHourOfDay());
                if (integer > 23) {
                    fieldErrors.put("dailyDigestHourOfDay", "Must be a number less than 24");
                }
            }
        } else {
            fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
        }

        if (StringUtils.isNotBlank(restModel.getPurgeDataFrequencyDays())) {
            if (!StringUtils.isNumeric(restModel.getPurgeDataFrequencyDays())) {
                fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
            } else {
                final Integer integer = Integer.valueOf(restModel.getPurgeDataFrequencyDays());
                if (integer > 8) {
                    fieldErrors.put("purgeDataFrequencyDays", "Must be a number less than 8");
                }
            }
        } else {
            fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String channelTestConfig(final GlobalSchedulingConfigRestModel restModel) throws IntegrationException {
        return null;
    }

    @Override
    public void configurationChangeTriggers(final GlobalSchedulingConfigRestModel restModel) {
        if (restModel != null) {
            final String dailyDigestHourOfDay = restModel.getDailyDigestHourOfDay();
            final String purgeDataFrequencyDays = restModel.getPurgeDataFrequencyDays();

            final String dailyDigestCron = String.format("0 0 %s 1/1 * ?", dailyDigestHourOfDay);
            final String purgeDataCron = String.format("0 0 0 1/%s * ?", purgeDataFrequencyDays);
            dailyDigestBatchConfig.scheduleExecution(dailyDigestCron);
            purgeConfig.scheduleExecution(purgeDataCron);
        }
    }
}
