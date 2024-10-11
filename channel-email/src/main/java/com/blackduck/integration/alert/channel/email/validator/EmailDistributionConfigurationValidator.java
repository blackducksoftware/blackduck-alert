/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;

@Component
public class EmailDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public EmailDistributionConfigurationValidator(CommonChannelDistributionValidator commonChannelDistributionValidator) {
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);

        configurationFieldValidator.validateRequiredRelatedSet(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, EmailDescriptor.LABEL_ADDITIONAL_ADDRESSES,
            ChannelDescriptor.KEY_PROVIDER_TYPE,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID
        );

        boolean additionalEmailsOnly = configurationFieldValidator.getBooleanValue(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY)
            .orElse(false);
        boolean projectOwnerOnly = configurationFieldValidator.getBooleanValue(EmailDescriptor.KEY_PROJECT_OWNER_ONLY)
            .orElse(false);

        if (additionalEmailsOnly && projectOwnerOnly) {
            configurationFieldValidator.addValidationResults(
                AlertFieldStatus.error(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, String.format("Cannot be set if %s is already set", EmailDescriptor.LABEL_PROJECT_OWNER_ONLY)),
                AlertFieldStatus.error(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, String.format("Cannot be set if %s is already set", EmailDescriptor.LABEL_ADDITIONAL_ADDRESSES_ONLY))
            );
        }

        if (additionalEmailsOnly) {
            configurationFieldValidator.validateRequiredRelatedSet(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, EmailDescriptor.LABEL_ADDITIONAL_ADDRESSES,
                EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES);
        }

        return configurationFieldValidator.getValidationResults();
    }

}
