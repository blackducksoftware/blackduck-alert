/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Set;

import org.springframework.data.domain.Sort.Direction;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.DistributionWithAuditInfo;

public interface DistributionAccessor {

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames);

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoWithSearch(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames, String searchTerm);
}
