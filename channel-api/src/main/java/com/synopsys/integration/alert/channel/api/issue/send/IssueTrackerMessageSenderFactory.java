/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.io.Serializable;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface IssueTrackerMessageSenderFactory<D extends DistributionJobDetailsModel, T extends Serializable> {
    IssueTrackerMessageSender<T> createMessageSender(D distributionDetails) throws AlertException;

}
