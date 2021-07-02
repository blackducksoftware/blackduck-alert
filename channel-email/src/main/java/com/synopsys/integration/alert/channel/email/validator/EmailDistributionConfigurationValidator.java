/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

@Component
public class EmailDistributionConfigurationValidator implements DistributionConfigurationValidator {
    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_FREQUENCY);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);

        configurationFieldValidator.validateAllOrNoneSet(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, ChannelDistributionUIConfig.KEY_PROVIDER_NAME, ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);

        configurationFieldValidator.containsDisallowedRelatedField(EmailDistributionUIConfig.LABEL_ADDITIONAL_ADDRESSES_ONLY, EmailDescriptor.KEY_PROJECT_OWNER_ONLY);

        configurationFieldValidator.containsDisallowedRelatedField(EmailDistributionUIConfig.LABEL_PROJECT_OWNER_ONLY, EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY);
        boolean additionalEmailsOnly = configurationFieldValidator.getStringValue(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY)
                                           .map(Boolean::valueOf)
                                           .orElse(false);

        if (additionalEmailsOnly) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES);
        }

        return configurationFieldValidator.getValidationResults();
    }

}
