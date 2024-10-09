package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface JobDetailsAccessor<D extends DistributionJobDetailsModel> {
    Optional<D> retrieveDetails(UUID jobId);

}
