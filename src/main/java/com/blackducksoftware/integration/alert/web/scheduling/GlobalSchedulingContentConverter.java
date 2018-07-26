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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.scheduling.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.alert.web.model.Config;

@Component
public class GlobalSchedulingContentConverter extends DatabaseContentConverter {

    @Autowired
    public GlobalSchedulingContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public Config getRestModelFromJson(final String json) {
        return getContentConverter().getJsonContent(json, GlobalSchedulingConfigRestModel.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final Config restModel) {
        final GlobalSchedulingConfigRestModel schedulingRestModel = (GlobalSchedulingConfigRestModel) restModel;
        final GlobalSchedulingConfigEntity schedulingEntity = new GlobalSchedulingConfigEntity(schedulingRestModel.getDailyDigestHourOfDay(), schedulingRestModel.getPurgeDataFrequencyDays());
        addIdToEntityPK(schedulingRestModel.getId(), schedulingEntity);
        return schedulingEntity;
    }

    @Override
    public Config populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final GlobalSchedulingConfigEntity schedulingEntity = (GlobalSchedulingConfigEntity) entity;
        final GlobalSchedulingConfigRestModel schedulingRestModel = new GlobalSchedulingConfigRestModel();
        schedulingRestModel.setDailyDigestHourOfDay(schedulingEntity.getDailyDigestHourOfDay());
        schedulingRestModel.setPurgeDataFrequencyDays(schedulingEntity.getPurgeDataFrequencyDays());
        final String id = getContentConverter().getStringValue(schedulingEntity.getId());
        schedulingRestModel.setId(id);
        return schedulingRestModel;
    }

}
