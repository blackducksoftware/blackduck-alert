/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Set;

import org.springframework.data.domain.Sort.Direction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;

public interface DistributionAccessor {

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames);

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoWithSearch(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames, String searchTerm);
}
