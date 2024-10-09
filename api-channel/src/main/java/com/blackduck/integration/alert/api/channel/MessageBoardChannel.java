/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.audit.AuditSuccessEvent;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

/**
 * @param <D> The type of job details relevant to this channel.
 * @param <T> The model containing all the message-fields this channel's implementation of {@link ChannelMessageSender} requires.
 *            This is meant to tightly couple the output of {@link AbstractChannelMessageConverter} to the input of {@link ChannelMessageSender}.
 */
public abstract class MessageBoardChannel<D extends DistributionJobDetailsModel, T> implements DistributionChannel<D> {
    private final AbstractChannelMessageConverter<D, T> channelMessageConverter;
    private final ChannelMessageSender<D, T, MessageResult> channelMessageSender;
    private final EventManager eventManager;
    private final ExecutingJobManager executingJobManager;

    protected MessageBoardChannel(
        AbstractChannelMessageConverter<D, T> channelMessageConverter,
        ChannelMessageSender<D, T, MessageResult> channelMessageSender,
        EventManager eventManager,
        ExecutingJobManager executingJobManager
    ) {
        this.channelMessageConverter = channelMessageConverter;
        this.channelMessageSender = channelMessageSender;
        this.eventManager = eventManager;
        this.executingJobManager = executingJobManager;
    }

    @Override
    public MessageResult distributeMessages(
        D distributionDetails,
        ProviderMessageHolder messages,
        String jobName,
        UUID jobConfigId,
        UUID jobExecutionId,
        Set<Long> notificationIds
    )
        throws AlertException {
        List<T> channelMessages = channelMessageConverter.convertToChannelMessages(distributionDetails, messages, jobName);
        MessageResult messageResult = channelMessageSender.sendMessages(distributionDetails, channelMessages);
        executingJobManager.incrementSentNotificationCount(jobExecutionId, notificationIds.size());
        eventManager.sendEvent(new AuditSuccessEvent(jobExecutionId, jobConfigId, notificationIds));
        return messageResult;
    }

}
