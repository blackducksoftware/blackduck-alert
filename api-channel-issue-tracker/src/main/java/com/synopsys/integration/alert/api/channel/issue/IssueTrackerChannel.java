/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

/**
 * @param <D> The type of job details relevant to this channel.
 * @param <T> The {@link Serializable} type of an issue-tracker issue's ID.
 */
public abstract class IssueTrackerChannel<D extends DistributionJobDetailsModel, T extends Serializable> implements DistributionChannel<D> {
    private final IssueTrackerProcessorFactory<D, T> processorFactory;
    private final IssueTrackerResponsePostProcessor responsePostProcessor;
    private final JobSubTaskAccessor jobSubTaskAccessor;

    protected IssueTrackerChannel(
        IssueTrackerProcessorFactory<D, T> processorFactory,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        JobSubTaskAccessor jobSubTaskAccessor
    ) {
        this.processorFactory = processorFactory;
        this.responsePostProcessor = responsePostProcessor;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
    }

    @Override
    public MessageResult distributeMessages(D distributionDetails, ProviderMessageHolder messages, String jobName, UUID jobExecutionId, Set<Long> notificationIds)
        throws AlertException {

        jobSubTaskAccessor.createSubTaskStatus(jobExecutionId, distributionDetails.getJobId(), 0L, notificationIds);
        IssueTrackerProcessor<T> processor = processorFactory.createProcessor(distributionDetails, jobExecutionId, notificationIds);
        IssueTrackerResponse<T> issueTrackerResponse = processor.processMessages(messages, jobName);

        return new MessageResult(issueTrackerResponse.getStatusMessage());
    }

}
