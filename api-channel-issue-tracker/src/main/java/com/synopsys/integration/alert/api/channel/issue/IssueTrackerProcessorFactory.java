/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue;

import java.io.Serializable;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface IssueTrackerProcessorFactory<D extends DistributionJobDetailsModel, T extends Serializable> {
    IssueTrackerProcessor<T> createProcessor(D distributionDetails) throws AlertException;

}
