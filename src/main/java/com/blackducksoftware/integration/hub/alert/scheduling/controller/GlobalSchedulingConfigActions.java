/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorProcessor;
import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorReader;
import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorWriter;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;

@Component
public class GlobalSchedulingConfigActions extends ConfigActions<GlobalSchedulingConfigEntity, GlobalSchedulingConfigRestModel, GlobalSchedulingRepository> {
    private final AccumulatorConfig accumulatorConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;
    private final PurgeConfig purgeConfig;

    private final GlobalProperties globalProperties;
    private final ChannelTemplateManager channelTemplateManager;
    private final NotificationManager notificationManager;
    private final List<NotificationTypeProcessor> processorList;

    @Autowired
    public GlobalSchedulingConfigActions(final AccumulatorConfig accumulatorConfig, final DailyDigestBatchConfig dailyDigestBatchConfig, final PurgeConfig purgeConfig, final GlobalSchedulingRepository repository,
            final ObjectTransformer objectTransformer, final GlobalProperties globalProperties, final ChannelTemplateManager channelTemplateManager, final NotificationManager notificationManager,
            final List<NotificationTypeProcessor> processorList) {
        super(GlobalSchedulingConfigEntity.class, GlobalSchedulingConfigRestModel.class, repository, objectTransformer);
        this.accumulatorConfig = accumulatorConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
        this.purgeConfig = purgeConfig;
        this.globalProperties = globalProperties;
        this.channelTemplateManager = channelTemplateManager;
        this.notificationManager = notificationManager;
        this.processorList = processorList;
    }

    @Override
    public List<GlobalSchedulingConfigRestModel> getConfig(final Long id) throws AlertException {
        Optional<GlobalSchedulingConfigEntity> databaseEntity = null;
        if (id != null) {
            databaseEntity = getRepository().findById(id);
        } else {
            final List<GlobalSchedulingConfigEntity> databaseEntities = getRepository().findAll();
            if (databaseEntities != null && !databaseEntities.isEmpty()) {
                databaseEntity = Optional.of(databaseEntities.get(0));
            }
        }
        GlobalSchedulingConfigRestModel restModel = null;
        if (databaseEntity != null) {
            restModel = getObjectTransformer().databaseEntityToConfigRestModel(databaseEntity.get(), getConfigRestModelClass());
            restModel.setDailyDigestNextRun(dailyDigestBatchConfig.getFormatedNextRunTime());
            restModel.setPurgeDataNextRun(purgeConfig.getFormatedNextRunTime());
        } else {
            restModel = new GlobalSchedulingConfigRestModel();
        }
        final Long accumulatorNextRun = accumulatorConfig.getMillisecondsToNextRun();
        if (accumulatorNextRun != null) {
            final Long seconds = TimeUnit.MILLISECONDS.toSeconds(accumulatorConfig.getMillisecondsToNextRun());
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

    public void runAccumulator() throws Exception {
        final AccumulatorReader reader = new AccumulatorReader(globalProperties);
        final AccumulatorProcessor processor = new AccumulatorProcessor(globalProperties, processorList);
        final AccumulatorWriter writer = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final NotificationDetailResults results = reader.read();
        final DBStoreEvent event = processor.process(results);
        final List<DBStoreEvent> events = new ArrayList<>();
        if (event != null) {
            events.add(event);
        }
        writer.write(events);
    }

}
