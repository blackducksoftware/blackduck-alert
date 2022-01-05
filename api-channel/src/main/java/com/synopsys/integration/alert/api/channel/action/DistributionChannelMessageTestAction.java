/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.action;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.DistributionChannelTestAction;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public abstract class DistributionChannelMessageTestAction<D extends DistributionJobDetailsModel> extends DistributionChannelTestAction {
    public static final String DEFAULT_TOPIC = "Alert Test Topic";
    public static final String DEFAULT_MESSAGE = "Alert Test Message";

    private final DistributionChannel<D> distributionChannel;

    public DistributionChannelMessageTestAction(ChannelKey channelKey, DistributionChannel<D> distributionChannel) {
        super(channelKey);
        this.distributionChannel = distributionChannel;
    }

    @Override
    public final MessageResult testConfig(DistributionJobModel testJobModel, String jobName, @Nullable String customTopic, @Nullable String customMessage) throws AlertException {
        String topicString = Optional.ofNullable(customTopic).orElse(DEFAULT_TOPIC);
        String messageString = Optional.ofNullable(customMessage).orElse(DEFAULT_MESSAGE);

        D distributionJobDetails = resolveTestDistributionDetails(testJobModel);
        ProviderMessageHolder messages = createTestMessageHolder(testJobModel, topicString, messageString);
        return distributionChannel.distributeMessages(distributionJobDetails, messages, jobName);
    }

    protected D resolveTestDistributionDetails(DistributionJobModel testJobModel) throws AlertException {
        return (D) testJobModel.getDistributionJobDetails();
    }

    private ProviderMessageHolder createTestMessageHolder(DistributionJobModel testJobModel, String summary, String message) {
        // TODO determine if it's worth it to make a DB call to resolve the provider
        LinkableItem providerItem = new LinkableItem("Provider Label", "Provider Config Name");
        ProviderDetails providerDetails = new ProviderDetails(testJobModel.getBlackDuckGlobalConfigId(), providerItem);
        SimpleMessage testMessage = SimpleMessage.original(providerDetails, summary, message, List.of());
        return new ProviderMessageHolder(List.of(), List.of(testMessage));
    }

}
