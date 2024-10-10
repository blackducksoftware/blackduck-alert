/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.msteams.validator.MsTeamsDistributionConfigurationValidator;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;

@Component
public class MsTeamsDescriptor extends ChannelDescriptor {
    public static final String KEY_WEBHOOK = "channel.msteams.webhook";

    public static final String MSTEAMS_LABEL = "MS Teams";
    public static final String MSTEAMS_URL = "msteams";
    public static final String MSTEAMS_DESCRIPTION = "Configure MS Teams for Alert.";

    public static final String LABEL_WEBHOOK = "Webhook";

    private final MsTeamsDistributionConfigurationValidator distributionValidator;

    @Autowired
    public MsTeamsDescriptor(MsTeamsDistributionConfigurationValidator distributionValidator) {
        super(ChannelKeys.MS_TEAMS, Set.of(ConfigContextEnum.DISTRIBUTION));
        this.distributionValidator = distributionValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.empty();
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(distributionValidator);
    }

}
