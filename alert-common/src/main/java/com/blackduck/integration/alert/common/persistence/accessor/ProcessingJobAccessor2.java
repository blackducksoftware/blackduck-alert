package com.blackduck.integration.alert.common.persistence.accessor;

import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public interface ProcessingJobAccessor2 {
    AlertPagedModel<SimpleFilteredDistributionJobResponseModel> getMatchingEnabledJobsForNotifications(
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel,
        int pageOffset,
        int pageLimit
    );
}
