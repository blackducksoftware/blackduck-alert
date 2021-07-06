/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;

@Component
public class CommonChannelDistributionValidator {
    public void validate(ConfigurationFieldValidator configurationFieldValidator) {
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_FREQUENCY);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
    }

}
