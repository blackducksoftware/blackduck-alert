package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Set;

import org.springframework.data.domain.Sort.Direction;

import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.DistributionWithAuditInfo;

public interface DistributionAccessor {

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames);

    AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoWithSearch(int pageStart, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames, String searchTerm);
}
