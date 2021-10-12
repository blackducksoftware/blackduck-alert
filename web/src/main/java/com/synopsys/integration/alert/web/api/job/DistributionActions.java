/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public ActionResponse<AlertPagedModel<DistributionWithAuditInfo>> retrieveJobWithAuditInfo(int pageStart, int pageSize, String sortName) {
        Set<String> authorizedChannelDescriptorNames = findAuthorizedChannelDescriptorNames();

        if (authorizedChannelDescriptorNames.isEmpty()) {
            return ActionResponse.createForbiddenResponse();
        }

        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(pageStart, pageSize, sortName, authorizedChannelDescriptorNames);
        return new ActionResponse(HttpStatus.OK, distributionWithAuditInfo);
    }

    private Set<String> findAuthorizedChannelDescriptorNames() {
        return descriptorMap.getDescriptorKeys()
            .stream()
            .filter(key -> authorizationManager.hasReadPermission(ConfigContextEnum.DISTRIBUTION, key))
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toSet());
    }

}
