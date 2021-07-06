/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
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
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public EmailDistributionConfigurationValidator(CommonChannelDistributionValidator commonChannelDistributionValidator) {
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);

        configurationFieldValidator.validateRequiredRelatedSet(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, EmailDistributionUIConfig.LABEL_ADDITIONAL_ADDRESSES,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);

        configurationFieldValidator.validateBothNotSet(
            EmailDistributionUIConfig.LABEL_ADDITIONAL_ADDRESSES_ONLY, EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY,
            EmailDistributionUIConfig.LABEL_PROJECT_OWNER_ONLY, EmailDescriptor.KEY_PROJECT_OWNER_ONLY
        );

        boolean additionalEmailsOnly = configurationFieldValidator.getBooleanValue(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY)
                                           .orElse(false);

        if (additionalEmailsOnly) {
            configurationFieldValidator.validateRequiredFieldIsNotBlank(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES);
        }

        return configurationFieldValidator.getValidationResults();
    }

}
