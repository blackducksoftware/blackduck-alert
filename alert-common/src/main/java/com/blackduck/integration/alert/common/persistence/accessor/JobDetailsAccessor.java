/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface JobDetailsAccessor<D extends DistributionJobDetailsModel> {
    Optional<D> retrieveDetails(UUID jobId);

}
