/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.scheduling.validator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;

@Component
public class SchedulingConfigurationFieldModelValidator implements GlobalConfigurationFieldModelValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromFieldModel(fieldModel);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(List.of(
            SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY,
            SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS,
            SchedulingDescriptor.KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS
        ));

        configurationFieldValidator.validateIsAValidOption(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, getDigestHours());
        configurationFieldValidator.validateIsAValidOption(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, getPurgeFrequency());
        configurationFieldValidator.validateIsAValidOption(SchedulingDescriptor.KEY_PURGE_AUDIT_FAILED_FREQUENCY_DAYS, getPurgeAuditDays());

        return configurationFieldValidator.getValidationResults();
    }

    private Set<String> getDigestHours() {
        return Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
    }

    private Set<String> getPurgeAuditDays() {
        Set<String> validRange = new LinkedHashSet<>();
        for (int index = 1; index < 31; index++) {
            validRange.add(String.valueOf(index));
        }

        return validRange;
    }

    private Set<String> getPurgeFrequency() {
        return Set.of("1", "2", "3", "4", "5", "6", "7");
    }
}
