/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
