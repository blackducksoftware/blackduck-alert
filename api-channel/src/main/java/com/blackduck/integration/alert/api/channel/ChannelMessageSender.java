/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface ChannelMessageSender<D extends DistributionJobDetailsModel, M, R> {
    R sendMessages(D details, List<M> channelMessages) throws AlertException;

}
