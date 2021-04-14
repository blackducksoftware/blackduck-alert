package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ProcessingJobAccessor {
    AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByFilteredNotifications(FilteredDistributionJobRequestModel filteredDistributionJobRequestModel, int pageOffset, int pageLimit);
}
