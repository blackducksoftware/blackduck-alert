/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.scheduling.validator;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;

@Component
public class SchedulingConfigurationValidator implements GlobalConfigurationValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
            SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY,
            SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS
        ));

        configurationFieldValidator.validateIsAnOption(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, getDigestHours());
        configurationFieldValidator.validateIsAnOption(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, getPurgeFrequency());

        return configurationFieldValidator.getValidationResults();
    }

    private List<String> getDigestHours() {
        return List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
    }

    private List<String> getPurgeFrequency() {
        return List.of("1", "2", "3", "4", "5", "6", "7");
    }
}
