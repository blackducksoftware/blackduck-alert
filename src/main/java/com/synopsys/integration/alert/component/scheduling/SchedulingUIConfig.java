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

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class SchedulingUIConfig extends UIConfig {
    public static final String KEY_ACCUMULATOR_NEXT_RUN = "accumulator.next.run";
    public static final String KEY_DAILY_DIGEST_HOUR_OF_DAY = "daily.digest.hour";
    public static final String KEY_DAILY_DIGEST_NEXT_RUN = "daily.digest.next.run";
    public static final String KEY_PURGE_DATA_FREQUENCY_DAYS = "purge.data.frequency";
    public static final String KEY_PURGE_DATA_NEXT_RUN = "purge.data.next.run";

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Scheduling", "scheduling", SchedulingDescriptor.SCHEDULING_COMPONENT, "clock-o", true, createFields());
    }

    public List<ConfigField> createFields() {
        final ConfigField digestHour = new SelectConfigField(KEY_DAILY_DIGEST_HOUR_OF_DAY, "Daily digest hour of day", true, false, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "10", "11", "12"));
        final ConfigField purgeFrequency = new SelectConfigField(KEY_PURGE_DATA_FREQUENCY_DAYS, "Purge data frequency in days", true, false, Arrays.asList("1", "2", "3"));
        return Arrays.asList(digestHour, purgeFrequency);
    }

}
