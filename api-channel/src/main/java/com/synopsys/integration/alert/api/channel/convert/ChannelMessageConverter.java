/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public interface ChannelMessageConverter<D extends DistributionJobDetailsModel, T> {
    List<T> convertToChannelMessages(D distributionDetails, ProviderMessageHolder messages, String jobName);

}
