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
package com.synopsys.integration.alert.database.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;

@Component
public class SchedulingRepositoryAccessor extends RepositoryAccessor {
    private final SchedulingRepository repository;
    private final DailyTask dailyTask;
    private final PurgeTask purgeTask;

    @Autowired
    public SchedulingRepositoryAccessor(final SchedulingRepository repository, final DailyTask dailyTask, final PurgeTask purgeTask) {
        super(repository);
        this.repository = repository;
        this.dailyTask = dailyTask;
        this.purgeTask = purgeTask;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        SchedulingConfigEntity schedulingEntity = (SchedulingConfigEntity) entity;
        schedulingEntity = repository.save(schedulingEntity);
        if (schedulingEntity != null) {
            final String dailyDigestHourOfDay = schedulingEntity.getDailyDigestHourOfDay();
            final String purgeDataFrequencyDays = schedulingEntity.getPurgeDataFrequencyDays();

            final String dailyDigestCron = String.format("0 0 %s 1/1 * ?", dailyDigestHourOfDay);
            final String purgeDataCron = String.format("0 0 0 1/%s * ?", purgeDataFrequencyDays);
            dailyTask.scheduleExecution(dailyDigestCron);
            purgeTask.scheduleExecution(purgeDataCron);
        }

        return schedulingEntity;
    }

}
