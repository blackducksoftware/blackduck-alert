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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

@Component
public class SchedulingUIConfig extends UIConfig {
    public static final String KEY_ACCUMULATOR_NEXT_RUN = "scheduling.accumulator.next.run";
    public static final String KEY_DAILY_DIGEST_HOUR_OF_DAY = "scheduling.daily.digest.hour";
    public static final String KEY_DAILY_DIGEST_NEXT_RUN = "scheduling.daily.digest.next.run";
    public static final String KEY_PURGE_DATA_FREQUENCY_DAYS = "scheduling.purge.data.frequency";
    public static final String KEY_PURGE_DATA_NEXT_RUN = "scheduling.purge.data.next.run";

    public SchedulingUIConfig() {
        super(SchedulingDescriptor.SCHEDULING_LABEL, SchedulingDescriptor.SCHEDULING_URL, SchedulingDescriptor.SCHEDULING_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField digestHour = SelectConfigField.createRequired(KEY_DAILY_DIGEST_HOUR_OF_DAY, "Daily digest hour of day", List.of("1", "2", "3", "4", "5", "6", "7", "8", "10", "11", "12"), this::validateDigestHourOfDay);
        final ConfigField purgeFrequency = SelectConfigField.createRequired(KEY_PURGE_DATA_FREQUENCY_DAYS, "Purge data frequency in days", List.of("1", "2", "3"), this::validatePurgeFrequency);
        return Arrays.asList(digestHour, purgeFrequency);
    }

    private Collection<String> validateDigestHourOfDay(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String dailyDigestHourOfDay = fieldToValidate.getValue().orElse(null);
        if (isNotValid(dailyDigestHourOfDay, 0, 23)) {
            return List.of("Must be a number between 0 and 23");
        }
        return List.of();
    }

    private Collection<String> validatePurgeFrequency(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String purgeDataFrequency = fieldToValidate.getValue().orElse(null);
        if (isNotValid(purgeDataFrequency, 1, 7)) {
            return List.of("Must be a number between 1 and 7");
        }
        return List.of();
    }

    private boolean isNotValid(final String actualValue, final Integer minimumAllowedValue, final Integer maximumAllowedValue) {
        return StringUtils.isBlank(actualValue) || !StringUtils.isNumeric(actualValue) || isOutOfRange(Integer.valueOf(actualValue), minimumAllowedValue, maximumAllowedValue);
    }

    private boolean isOutOfRange(final Integer number, final Integer minimumAllowedValue, final Integer maximumAllowedValue) {
        return number < minimumAllowedValue || maximumAllowedValue < number;
    }

}
