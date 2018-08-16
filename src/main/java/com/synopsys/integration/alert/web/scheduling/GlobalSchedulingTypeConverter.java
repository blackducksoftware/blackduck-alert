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
package com.synopsys.integration.alert.web.scheduling;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.OnDemandTask;

@Component
public class GlobalSchedulingTypeConverter extends TypeConverter {
    private final PurgeTask purgeTask;
    private final DailyTask dailyTask;
    private final OnDemandTask onDemandTask;

    @Autowired
    public GlobalSchedulingTypeConverter(final ContentConverter contentConverter, final DailyTask dailyTask, final OnDemandTask onDemandTask, final PurgeTask purgeTask) {
        super(contentConverter);
        this.purgeTask = purgeTask;
        this.dailyTask = dailyTask;
        this.onDemandTask = onDemandTask;
    }

    @Override
    public Config getConfigFromJson(final String json) {
        return getContentConverter().getJsonContent(json, GlobalSchedulingConfig.class);
    }

    @Override
    public DatabaseEntity populateEntityFromConfig(final Config restModel) {
        final GlobalSchedulingConfig schedulingRestModel = (GlobalSchedulingConfig) restModel;
        final GlobalSchedulingConfigEntity schedulingEntity = new GlobalSchedulingConfigEntity(schedulingRestModel.getDailyDigestHourOfDay(), schedulingRestModel.getPurgeDataFrequencyDays());
        addIdToEntityPK(schedulingRestModel.getId(), schedulingEntity);
        return schedulingEntity;
    }

    @Override
    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        final GlobalSchedulingConfigEntity schedulingEntity = (GlobalSchedulingConfigEntity) entity;
        final GlobalSchedulingConfig schedulingRestModel = new GlobalSchedulingConfig();
        schedulingRestModel.setDailyDigestHourOfDay(schedulingEntity.getDailyDigestHourOfDay());
        schedulingRestModel.setPurgeDataFrequencyDays(schedulingEntity.getPurgeDataFrequencyDays());
        final String id = getContentConverter().getStringValue(schedulingEntity.getId());
        schedulingRestModel.setId(id);
        return addConfigNextRunData(schedulingRestModel);
    }

    private GlobalSchedulingConfig addConfigNextRunData(final GlobalSchedulingConfig schedulingRestModel) {
        schedulingRestModel.setDailyDigestNextRun(dailyTask.getFormatedNextRunTime().orElse(null));
        schedulingRestModel.setPurgeDataNextRun(purgeTask.getFormatedNextRunTime().orElse(null));
        final Optional<Long> onDemandNextRun = onDemandTask.getMillisecondsToNextRun();
        if (onDemandNextRun.isPresent()) {
            final Long seconds = TimeUnit.MILLISECONDS.toSeconds(onDemandNextRun.get());
            schedulingRestModel.setAccumulatorNextRun(String.valueOf(seconds));
        }
        return schedulingRestModel;
    }

}
