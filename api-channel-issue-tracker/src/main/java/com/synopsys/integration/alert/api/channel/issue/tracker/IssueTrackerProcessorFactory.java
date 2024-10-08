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

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface IssueTrackerProcessorFactory<D extends DistributionJobDetailsModel, T extends Serializable> {
    IssueTrackerProcessor<T> createProcessor(D distributionDetails, UUID jobExecutionId, Set<Long> notificationIds) throws AlertException;

}
