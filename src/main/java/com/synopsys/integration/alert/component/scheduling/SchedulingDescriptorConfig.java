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
package com.synopsys.integration.alert.component.scheduling;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.scheduling.SchedulingReposioryAccessor;
import com.synopsys.integration.alert.web.component.scheduling.SchedulingConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SchedulingDescriptorConfig extends DescriptorConfig {

    @Autowired
    public SchedulingDescriptorConfig(final SchedulingTypeConverter typeConverter, final SchedulingReposioryAccessor repositoryAccessor) {
        super(typeConverter, repositoryAccessor);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Scheduling", "scheduling", "clock-o", Collections.emptyList());
    }

    @Override
    public void validateConfig(final Config config, final Map<String, String> fieldErrors) {
        final SchedulingConfig schedulingConfig = (SchedulingConfig) config;
        if (StringUtils.isNotBlank(schedulingConfig.getDailyDigestHourOfDay())) {
            if (!StringUtils.isNumeric(schedulingConfig.getDailyDigestHourOfDay())) {
                fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
            } else {
                final Integer integer = Integer.valueOf(schedulingConfig.getDailyDigestHourOfDay());
                if (integer > 23) {
                    fieldErrors.put("dailyDigestHourOfDay", "Must be a number less than 24");
                }
            }
        } else {
            fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
        }

        if (StringUtils.isNotBlank(schedulingConfig.getPurgeDataFrequencyDays())) {
            if (!StringUtils.isNumeric(schedulingConfig.getPurgeDataFrequencyDays())) {
                fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
            } else {
                final Integer integer = Integer.valueOf(schedulingConfig.getPurgeDataFrequencyDays());
                if (integer > 8) {
                    fieldErrors.put("purgeDataFrequencyDays", "Must be a number less than 8");
                }
            }
        } else {
            fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        throw new IntegrationException("Should not be implemented");
    }

}
