/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.UUID;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

//TODO Move these methods into JobDetailsAccessor when all implementors of the existing JobDetailsAccessor have been changed to use this interface
public interface JobDetailsAccessor2<D extends DistributionJobDetailsModel> extends JobDetailsAccessor<D> {
    DescriptorKey getDescriptorKey();

    D saveJobDetails(UUID jobId, DistributionJobDetailsModel jobDetailsModel);

    D saveConcreteJobDetails(UUID jobId, D jobDetails);
}
