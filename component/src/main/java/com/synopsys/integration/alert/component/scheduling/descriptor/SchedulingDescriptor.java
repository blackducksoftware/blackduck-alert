/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.component.scheduling.validator.SchedulingConfigurationFieldModelValidator;

@Component
public class SchedulingDescriptor extends ComponentDescriptor {
    public static final String SCHEDULING_LABEL = "Scheduling";
    public static final String SCHEDULING_URL = "scheduling";
    public static final String SCHEDULING_DESCRIPTION = "This page shows when system scheduled tasks will run next, as well as allow you to configure the frequency of the system tasks.";

    public static final String KEY_DAILY_PROCESSOR_HOUR_OF_DAY = "scheduling.daily.processor.hour";
    public static final String KEY_DAILY_PROCESSOR_NEXT_RUN = "scheduling.daily.processor.next.run";
    public static final String KEY_PURGE_DATA_FREQUENCY_DAYS = "scheduling.purge.data.frequency";
    public static final String KEY_PURGE_DATA_NEXT_RUN = "scheduling.purge.data.next.run";

    private final SchedulingConfigurationFieldModelValidator schedulingValidator;

    @Autowired
    public SchedulingDescriptor(SchedulingDescriptorKey schedulingDescriptorKey, SchedulingConfigurationFieldModelValidator schedulingValidator) {
        super(schedulingDescriptorKey);
        this.schedulingValidator = schedulingValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(schedulingValidator);
    }

}
