/*
 * channel-slack
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.validator.SlackDistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class SlackDescriptor extends ChannelDescriptor {
    public static final String SLACK_PREFIX = "slack.";
    public static final String SLACK_CHANNEL_PREFIX = "channel." + SLACK_PREFIX;

    public static final String KEY_WEBHOOK = SLACK_CHANNEL_PREFIX + "webhook";
    public static final String KEY_CHANNEL_NAME = SLACK_CHANNEL_PREFIX + "channel.name";
    public static final String KEY_CHANNEL_USERNAME = SLACK_CHANNEL_PREFIX + "channel.username";

    public static final String SLACK_LABEL = "Slack";
    public static final String SLACK_URL = "slack";
    public static final String SLACK_DESCRIPTION = "Configure Slack for Alert.";

    public static final String LABEL_WEBHOOK = "Webhook";
    public static final String LABEL_SLACK_CHANNEL_NAME = "Channel Name";
    public static final String LABEL_CHANNEL_USERNAME = "Channel Username";

    private final SlackDistributionConfigurationValidator distributionValidator;

    @Autowired
    public SlackDescriptor(SlackDistributionConfigurationValidator distributionValidator) {
        super(ChannelKeys.SLACK, Set.of(ConfigContextEnum.DISTRIBUTION));
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
