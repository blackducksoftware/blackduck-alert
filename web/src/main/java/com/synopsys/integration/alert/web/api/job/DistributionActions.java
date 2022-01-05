/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DistributionActions {
    private final DistributionAccessor distributionAccessor;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    @Autowired
    public DistributionActions(DistributionAccessor distributionAccessor, AuthorizationManager authorizationManager, DescriptorMap descriptorMap) {
        this.distributionAccessor = distributionAccessor;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    public ActionResponse<AlertPagedModel<DistributionWithAuditInfo>> retrieveJobWithAuditInfo(int page, int pageSize, String sortName, @Nullable String sortOrder, @Nullable String searchTerm) {
        Set<String> authorizedChannelDescriptorNames = findAuthorizedChannelDescriptorNames();

        if (authorizedChannelDescriptorNames.isEmpty()) {
            return ActionResponse.createForbiddenResponse();
        }

        String applicableSortName = convertSortName(sortName);
        Sort.Direction validSortOrder = Sort.Direction.ASC;
        if (Sort.Direction.DESC.name().equalsIgnoreCase(sortOrder)) {
            validSortOrder = Sort.Direction.DESC;
        }
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo;
        if (searchTerm != null) {
            distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfoWithSearch(page, pageSize, applicableSortName, validSortOrder, authorizedChannelDescriptorNames, searchTerm);
        } else {
            distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(page, pageSize, applicableSortName, validSortOrder, authorizedChannelDescriptorNames);
        }

        return new ActionResponse(HttpStatus.OK, distributionWithAuditInfo);
    }

    private Set<String> findAuthorizedChannelDescriptorNames() {
        return descriptorMap.getDescriptorKeys()
            .stream()
            .filter(key -> authorizationManager.hasReadPermission(ConfigContextEnum.DISTRIBUTION, key))
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toSet());
    }

    private String convertSortName(String sortName) {
        switch (sortName) {
            case "channel":
                return "channelDescriptorName";
            case "frequency":
                return "distributionFrequency";
            case "lastSent":
                return "lastSent";
            case "status":
                return "audit.status";
            default:
                return "name";
        }
    }

}
