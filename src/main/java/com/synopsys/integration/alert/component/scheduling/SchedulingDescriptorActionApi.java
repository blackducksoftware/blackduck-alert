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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SchedulingDescriptorActionApi extends DescriptorActionApi {

    @Override
    public void validateConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String dailyDigestHourOfDay = fieldAccessor.getString(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY).orElse(null);
        final String purgeDataFrequency = fieldAccessor.getString(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS).orElse(null);

        if (StringUtils.isNotBlank(dailyDigestHourOfDay)) {
            if (!StringUtils.isNumeric(dailyDigestHourOfDay)) {
                fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
            } else {
                final Integer integer = Integer.valueOf(dailyDigestHourOfDay);
                if (integer > 23) {
                    fieldErrors.put("dailyDigestHourOfDay", "Must be a number less than 24");
                }
            }
        } else {
            fieldErrors.put("dailyDigestHourOfDay", "Must be a number between 0 and 23");
        }

        if (StringUtils.isNotBlank(purgeDataFrequency)) {
            if (!StringUtils.isNumeric(purgeDataFrequency)) {
                fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
            } else {
                final Integer integer = Integer.valueOf(purgeDataFrequency);
                if (integer > 8) {
                    fieldErrors.put("purgeDataFrequencyDays", "Must be a number less than 8");
                }
            }
        } else {
            fieldErrors.put("purgeDataFrequencyDays", "Must be a number between 1 and 7");
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        throw new IntegrationException("Should not be implemented");
    }

}
