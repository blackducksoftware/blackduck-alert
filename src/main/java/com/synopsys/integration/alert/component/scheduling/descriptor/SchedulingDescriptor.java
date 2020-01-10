/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.scheduling.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;

@Component
public class SchedulingDescriptor extends ComponentDescriptor {
    public static final String SCHEDULING_LABEL = "Scheduling";
    public static final String SCHEDULING_URL = "scheduling";
    public static final String SCHEDULING_DESCRIPTION = "This page shows when the scheduled tasks will run next as well as allow you to configure the frequency of the tasks.";

    public static final String KEY_BLACKDUCK_NEXT_RUN = "scheduling.accumulator.next.run";
    public static final String KEY_POLARIS_NEXT_RUN = "scheduling.polaris.next.run";
    public static final String KEY_DAILY_PROCESSOR_HOUR_OF_DAY = "scheduling.daily.processor.hour";
    public static final String KEY_DAILY_PROCESSOR_NEXT_RUN = "scheduling.daily.processor.next.run";
    public static final String KEY_PURGE_DATA_FREQUENCY_DAYS = "scheduling.purge.data.frequency";
    public static final String KEY_PURGE_DATA_NEXT_RUN = "scheduling.purge.data.next.run";

    @Autowired
    public SchedulingDescriptor(SchedulingDescriptorKey schedulingDescriptorKey, SchedulingUIConfig schedulingUIConfig) {
        super(schedulingDescriptorKey, schedulingUIConfig);
    }

}
