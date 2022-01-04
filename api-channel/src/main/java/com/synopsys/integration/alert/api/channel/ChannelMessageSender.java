/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface ChannelMessageSender<D extends DistributionJobDetailsModel, M, R> {
    R sendMessages(D details, List<M> channelMessages) throws AlertException;

}
