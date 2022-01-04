/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.CommonProviderDistributionValidator;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@Component
public class BlackDuckDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final CommonProviderDistributionValidator commonProviderDistributionValidator;

    @Autowired
    public BlackDuckDistributionConfigurationValidator(CommonProviderDistributionValidator commonProviderDistributionValidator) {
        this.commonProviderDistributionValidator = commonProviderDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonProviderDistributionValidator.validate(configurationFieldValidator);

        configurationFieldValidator.validateRequiredRelatedSet(
            BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, BlackDuckDescriptor.LABEL_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER,
            ProviderDescriptor.KEY_NOTIFICATION_TYPES,
            ChannelDescriptor.KEY_PROVIDER_TYPE,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID
        );

        configurationFieldValidator.validateRequiredRelatedSet(
            BlackDuckDescriptor.KEY_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER, BlackDuckDescriptor.LABEL_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER,
            ProviderDescriptor.KEY_NOTIFICATION_TYPES
        );

        return configurationFieldValidator.getValidationResults();
    }

}
