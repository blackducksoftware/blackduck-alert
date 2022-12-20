/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.audit.AuditSuccessEvent;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

/**
 * @param <D> The type of job details relevant to this channel.
 * @param <T> The model containing all the message-fields this channel's implementation of {@link ChannelMessageSender} requires.
 *            This is meant to tightly couple the output of {@link AbstractChannelMessageConverter} to the input of {@link ChannelMessageSender}.
 */
public abstract class MessageBoardChannel<D extends DistributionJobDetailsModel, T> implements DistributionChannel<D> {
    private final AbstractChannelMessageConverter<D, T> channelMessageConverter;
    private final ChannelMessageSender<D, T, MessageResult> channelMessageSender;
    private final EventManager eventManager;

    protected MessageBoardChannel(
        AbstractChannelMessageConverter<D, T> channelMessageConverter,
        ChannelMessageSender<D, T, MessageResult> channelMessageSender,
        EventManager eventManager
    ) {
        this.channelMessageConverter = channelMessageConverter;
        this.channelMessageSender = channelMessageSender;
        this.eventManager = eventManager;
    }

    @Override
    public MessageResult distributeMessages(D distributionDetails, ProviderMessageHolder messages, String jobName, UUID jobExecutionId, Set<Long> notificationIds)
        throws AlertException {
        List<T> channelMessages = channelMessageConverter.convertToChannelMessages(distributionDetails, messages, jobName);
        MessageResult messageResult = channelMessageSender.sendMessages(distributionDetails, channelMessages);
        eventManager.sendEvent(new AuditSuccessEvent(distributionDetails.getJobId(), notificationIds));
        return messageResult;
    }

}
