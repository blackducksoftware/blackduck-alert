/*
 * channel-msteams
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.msteams.validator.MsTeamsDistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class MsTeamsDescriptor extends ChannelDescriptor {
    public static final String KEY_WEBHOOK = "channel.msteams.webhook";

    public static final String MSTEAMS_LABEL = "MS Teams";
    public static final String MSTEAMS_URL = "msteams";
    public static final String MSTEAMS_DESCRIPTION = "Configure MS Teams for Alert.";
    private final MsTeamsDistributionConfigurationValidator distributionValidator;

    @Autowired
    public MsTeamsDescriptor(MsTeamsUIConfig msTeamsUIConfig, MsTeamsGlobalUIConfig msTeamsGlobalUIConfig, MsTeamsDistributionConfigurationValidator distributionValidator) {
        super(ChannelKeys.MS_TEAMS, msTeamsUIConfig, msTeamsGlobalUIConfig);
        this.distributionValidator = distributionValidator;
    }

    @Override
    public Optional<GlobalConfigurationValidator> getGlobalValidator() {
        return Optional.empty();
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(distributionValidator);
    }
}
