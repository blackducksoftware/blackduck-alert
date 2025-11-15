/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.springframework.lang.Nullable;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface IssueTrackerMessageSenderFactory<D extends DistributionJobDetailsModel, T extends Serializable, A extends Serializable> {
    IssueTrackerMessageSender<T> createMessageSender(D distributionDetails, @Nullable UUID globalId) throws AlertException;

    AsyncMessageSender<A> createAsyncMessageSender(
        D distributionDetails, @Nullable UUID globalId, UUID jobExecutionId,
        Set<Long> notificationIds
    ) throws AlertException;

}
