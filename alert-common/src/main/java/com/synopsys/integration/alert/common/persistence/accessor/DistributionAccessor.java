/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;

public interface DistributionAccessor {

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int pageStart, int pageSize, String sortName);

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoGavin(int pageStart, int pageSize, String sortName);

    AlertPagedModel<DistributionWithAuditInfo> getDistributionJobModel(int pageStart, int pageSize, String sortName);
}
