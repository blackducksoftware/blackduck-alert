/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.component.scheduling.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.SchedulingUIConfig;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.database.deprecated.scheduling.SchedulingRepository;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
public class SchedulingUpgrade extends DataUpgrade {
    private final FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    public SchedulingUpgrade(final SchedulingRepository repository, final BaseConfigurationAccessor configurationAccessor, final FieldCreatorUtil fieldCreatorUtil) {
        super(SchedulingDescriptor.SCHEDULING_COMPONENT, repository, ConfigContextEnum.GLOBAL, configurationAccessor);
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    @Override
    public List<ConfigurationFieldModel> convertEntityToFieldList(final DatabaseEntity databaseEntity) {
        final SchedulingConfigEntity entity = (SchedulingConfigEntity) databaseEntity;
        final List<ConfigurationFieldModel> fieldModels = new LinkedList<>();

        final String dailyDigestHourOfDay = entity.getDailyDigestHourOfDay();
        final String purgeDataFrequencyDays = entity.getPurgeDataFrequencyDays();

        fieldCreatorUtil.addFieldModel(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY, dailyDigestHourOfDay, fieldModels);
        fieldCreatorUtil.addFieldModel(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS, purgeDataFrequencyDays, fieldModels);

        return fieldModels;
    }
}
