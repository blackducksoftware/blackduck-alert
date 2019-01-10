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
package com.synopsys.integration.alert.component.scheduling;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SchedulingDescriptorActionApi extends DescriptorActionApi {

    @Override
    public void validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final Optional<FieldValueModel> optionalDigestHourOfDay = fieldModel.getField(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY);
        final Optional<FieldValueModel> optionalPurgeDataFrequency = fieldModel.getField(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS);

        if (optionalDigestHourOfDay.isPresent()) {
            final String dailyDigestHourOfDay = optionalDigestHourOfDay.get().getValue().orElse(null);
            if (isNotValid(dailyDigestHourOfDay, 0, 23)) {
                fieldErrors.put(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY, "Must be a number between 0 and 23");
            }
        }

        if (optionalPurgeDataFrequency.isPresent()) {
            final String purgeDataFrequency = optionalPurgeDataFrequency.get().getValue().orElse(null);
            if (isNotValid(purgeDataFrequency, 1, 7)) {
                fieldErrors.put(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS, "Must be a number between 1 and 7");
            }
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        throw new IntegrationException("Should not be implemented");
    }

    private boolean isNotValid(final String actualValue, final Integer minimumAllowedValue, final Integer maximumAllowedValue) {
        return StringUtils.isBlank(actualValue) || !StringUtils.isNumeric(actualValue) || isOutOfRange(Integer.valueOf(actualValue), minimumAllowedValue, maximumAllowedValue);
    }

    private boolean isOutOfRange(final Integer number, final Integer minimumAllowedValue, final Integer maximumAllowedValue) {
        return number < minimumAllowedValue || maximumAllowedValue < number;
    }

}
