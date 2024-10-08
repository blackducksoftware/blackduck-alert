/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.job;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class DistributionActions {
    private static final String CHANNEL_DESCRIPTOR_SORT_NAME = "channel_descriptor_name";

    private final DistributionAccessor distributionAccessor;
    private final AuthorizationManager authorizationManager;
    private final DescriptorMap descriptorMap;

    @Autowired
    public DistributionActions(DistributionAccessor distributionAccessor, AuthorizationManager authorizationManager, DescriptorMap descriptorMap) {
        this.distributionAccessor = distributionAccessor;
        this.authorizationManager = authorizationManager;
        this.descriptorMap = descriptorMap;
    }

    public ActionResponse<AlertPagedModel<DistributionWithAuditInfo>> retrieveJobWithAuditInfo(int page, int pageSize, @Nullable String sortName, @Nullable String sortOrder, @Nullable String searchTerm) {
        Set<String> authorizedChannelDescriptorNames = findAuthorizedChannelDescriptorNames();

        if (authorizedChannelDescriptorNames.isEmpty()) {
            return ActionResponse.createForbiddenResponse();
        }

        String applicableSortName = convertSortName(sortName);
        Direction validSortOrder = Direction.ASC;
        if (Direction.DESC.name().equalsIgnoreCase(sortOrder)) {
            validSortOrder = Direction.DESC;
        }
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo;
        if (searchTerm != null) {
            distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfoWithSearch(page, pageSize, applicableSortName, validSortOrder, authorizedChannelDescriptorNames, searchTerm);
        } else {
            distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(page, pageSize, applicableSortName, validSortOrder, authorizedChannelDescriptorNames);
        }

        AlertPagedModel<DistributionWithAuditInfo> sortedPagedModel = sortByChannelDisplayNameIfApplicable(distributionWithAuditInfo, applicableSortName, validSortOrder);
        return new ActionResponse(HttpStatus.OK, sortedPagedModel);
    }

    private AlertPagedModel<DistributionWithAuditInfo> sortByChannelDisplayNameIfApplicable(AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo, String sortName, Direction sortDirection) {
        if (!CHANNEL_DESCRIPTOR_SORT_NAME.equals(sortName)) {
            return distributionWithAuditInfo;
        }

        List<DistributionWithAuditInfo> sortedInfos = distributionWithAuditInfo.getModels().stream().sorted((left, right) -> {
            Comparator sorter = (Direction.DESC.equals(sortDirection)) ? Comparator.reverseOrder() : Comparator.naturalOrder();
            String leftSideName = descriptorMap.getDescriptorKey(left.getChannelName()).map(DescriptorKey::getDisplayName).orElse(null);
            String rightSideName = descriptorMap.getDescriptorKey(right.getChannelName()).map(DescriptorKey::getDisplayName).orElse(null);
            if (null == leftSideName || null == rightSideName) {
                return 0;
            }

            return sorter.compare(leftSideName, rightSideName);
        }).collect(Collectors.toList());

        return new AlertPagedModel<>(
            distributionWithAuditInfo.getTotalPages(),
            distributionWithAuditInfo.getCurrentPage(),
            distributionWithAuditInfo.getPageSize(),
            sortedInfos
        );
    }

    private Set<String> findAuthorizedChannelDescriptorNames() {
        return descriptorMap.getDescriptorKeys()
            .stream()
            .filter(key -> authorizationManager.hasReadPermission(ConfigContextEnum.DISTRIBUTION, key))
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toSet());
    }

    private String convertSortName(String sortName) {
        if (StringUtils.isBlank(sortName)) {
            return "name";
        }

        switch (sortName) {
            case "channel":
                return CHANNEL_DESCRIPTOR_SORT_NAME;
            case "frequency":
                return "distribution_frequency";
            default:
                return "name";
        }
    }

}
