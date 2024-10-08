/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

/**
 * @param <D> The type of job details relevant to this channel.
 * @param <T> The {@link Serializable} type of an issue-tracker issue's ID.
 */
public abstract class IssueTrackerChannel<D extends DistributionJobDetailsModel, T extends Serializable> implements DistributionChannel<D> {
    private final IssueTrackerProcessorFactory<D, T> processorFactory;

    protected IssueTrackerChannel(
        IssueTrackerProcessorFactory<D, T> processorFactory
    ) {
        this.processorFactory = processorFactory;
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

        IssueTrackerProcessor<T> processor = processorFactory.createProcessor(distributionDetails, jobExecutionId, notificationIds);
        IssueTrackerResponse<T> issueTrackerResponse = processor.processMessages(messages, jobName);

        return new MessageResult(issueTrackerResponse.getStatusMessage());
    }

}
