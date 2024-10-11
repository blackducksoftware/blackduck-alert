/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.convert;

import java.util.List;

import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface ChannelMessageConverter<D extends DistributionJobDetailsModel, T> {
    List<T> convertToChannelMessages(D distributionDetails, ProviderMessageHolder messages, String jobName);

}
