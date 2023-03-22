/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.send;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.springframework.lang.Nullable;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface IssueTrackerMessageSenderFactory<D extends DistributionJobDetailsModel, T extends Serializable> {
    IssueTrackerMessageSender<T> createMessageSender(D distributionDetails, @Nullable UUID globalId) throws AlertException;

    IssueTrackerAsyncMessageSender<T> createAsyncMessageSender(
        D distributionDetails, @Nullable UUID globalId, UUID jobExecutionId,
        Set<Long> notificationIds
    ) throws AlertException;

}
