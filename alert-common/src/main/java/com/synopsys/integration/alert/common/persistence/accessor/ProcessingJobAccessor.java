package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModelV2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public interface ProcessingJobAccessor {
    AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByFilteredNotifications(FilteredDistributionJobRequestModelV2 filteredDistributionJobRequestModel, int pageOffset, int pageLimit);
}
