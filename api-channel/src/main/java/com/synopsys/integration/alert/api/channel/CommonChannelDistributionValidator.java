/*
 * api-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;

@Component
public class CommonChannelDistributionValidator {
    public void validate(ConfigurationFieldValidator configurationFieldValidator) {
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_NAME);
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_FREQUENCY);

        Set<String> validValues = Arrays.stream(FrequencyType.values())
                                           .map(FrequencyType::name)
                                            .collect(Collectors.toSet());
        configurationFieldValidator.validateIsAValidOption(ChannelDistributionUIConfig.KEY_FREQUENCY, validValues);

        configurationFieldValidator.validateRequiredFieldIsNotBlank(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
    }

}
