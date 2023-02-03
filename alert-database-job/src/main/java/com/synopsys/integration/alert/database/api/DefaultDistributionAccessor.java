/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.distribution.DistributionRepository;
import com.synopsys.integration.alert.database.distribution.DistributionRepository.DistributionDBResponse;

@Component
public class DefaultDistributionAccessor implements DistributionAccessor {
    private final DistributionRepository distributionRepository;

    @Autowired
    public DefaultDistributionAccessor(DistributionRepository distributionRepository) {
        this.distributionRepository = distributionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int page, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames) {
        return retrieveData(page, pageSize, sortName, sortOrder, pageRequest -> distributionRepository.getDistributionWithAuditInfo(pageRequest, allowedDescriptorNames));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoWithSearch(int page, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames, String searchTerm) {
        return retrieveData(page, pageSize, sortName, sortOrder, (pageRequest -> distributionRepository.getDistributionWithAuditInfoWithSearch(pageRequest, allowedDescriptorNames, searchTerm)));
    }

    private AlertPagedModel<DistributionWithAuditInfo> retrieveData(int page, int pageSize, String sortName, Direction sortOrder, Function<PageRequest, Page<DistributionDBResponse>> retrieveData) {
        Sort sort = (sortName == null || sortOrder == null) ? Sort.unsorted() : Sort.by(sortOrder, sortName);
        PageRequest pageRequest = PageRequest.of(page, pageSize, sort);
        Page<DistributionDBResponse> pageOfResponses = retrieveData.apply(pageRequest);
        List<DistributionWithAuditInfo> distributionWithAuditInfos = pageOfResponses.get().map(this::convert).collect(Collectors.toList());
        return new AlertPagedModel<>(
            pageOfResponses.getTotalPages(),
            pageOfResponses.getNumber(),
            pageOfResponses.getSize(),
            distributionWithAuditInfos
        );
    }

    private DistributionWithAuditInfo convert(DistributionDBResponse distributionWithAuditEntity) {
        return new DistributionWithAuditInfo(
            UUID.fromString(distributionWithAuditEntity.getId()),
            distributionWithAuditEntity.getEnabled(),
            distributionWithAuditEntity.getName(),
            distributionWithAuditEntity.getChannel_Descriptor_Name(),
            FrequencyType.valueOf(distributionWithAuditEntity.getDistribution_Frequency()),
            formatAuditDate(distributionWithAuditEntity.getTime_Last_Sent()),
            distributionWithAuditEntity.getStatus()
        );
    }

    private String formatAuditDate(Timestamp dateTime) {
        if (dateTime == null) {
            return null;
        }

        return DateUtils.formatDate(DateUtils.fromInstantUTC(dateTime.toInstant()), DateUtils.AUDIT_DATE_FORMAT);
    }
}
