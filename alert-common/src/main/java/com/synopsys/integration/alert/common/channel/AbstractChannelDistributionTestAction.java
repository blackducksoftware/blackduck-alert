/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class AbstractChannelDistributionTestAction implements ChannelDistributionTestAction {
    private final DistributionChannel distributionChannel;

    public AbstractChannelDistributionTestAction(DistributionChannel distributionChannel) {
        this.distributionChannel = distributionChannel;
    }

    @Override
    public MessageResult testConfig(
        DistributionJobModel testJobModel,
        @Nullable ConfigurationModel channelGlobalConfig,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws IntegrationException {
        String topicString = Optional.ofNullable(customTopic).orElse("Alert Test Topic");
        String messageString = Optional.ofNullable(customMessage).orElse("Alert Test Message");
        DistributionEvent channelTestEvent = ChannelDistributionTestEventCreationUtils.createChannelTestEvent(topicString, messageString, testJobModel, channelGlobalConfig);
        return distributionChannel.sendMessage(channelTestEvent);
    }

    protected DistributionChannel getDistributionChannel() {
        return distributionChannel;
    }

}
