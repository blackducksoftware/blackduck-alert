/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.database.distribution.DistributionRepository;
import com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity;

@Component
public class DefaultDistributionAccessor implements DistributionAccessor {

    private final DistributionRepository distributionRepository;

    @Autowired
    public DefaultDistributionAccessor(DistributionRepository distributionRepository) {
        this.distributionRepository = distributionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int pageStart, int pageSize, String sortName, Set<String> allowedDescriptorNames) {
        PageRequest pageRequest = PageRequest.of(pageStart, pageSize, Sort.by(sortName));
        Page<DistributionWithAuditEntity> distributionWithAuditInfo = distributionRepository.getDistributionWithAuditInfo(pageRequest, allowedDescriptorNames);
        return convert(distributionWithAuditInfo);
    }

    private AlertPagedModel<DistributionWithAuditInfo> convert(Page<DistributionWithAuditEntity> pageOfDistributionWithAuditEntity) {
        List<DistributionWithAuditInfo> results = pageOfDistributionWithAuditEntity.get().map(this::convert).collect(Collectors.toList());
        return new AlertPagedModel<>(
            pageOfDistributionWithAuditEntity.getTotalPages(),
            pageOfDistributionWithAuditEntity.getNumber(),
            pageOfDistributionWithAuditEntity.getSize(),
            results
        );
    }

    private DistributionWithAuditInfo convert(DistributionWithAuditEntity distributionWithAuditEntity) {
        return new DistributionWithAuditInfo(
            distributionWithAuditEntity.getJobId(),
            distributionWithAuditEntity.isEnabled(),
            distributionWithAuditEntity.getJobName(),
            distributionWithAuditEntity.getChannelName(),
            FrequencyType.valueOf(distributionWithAuditEntity.getFrequencyType()),
            distributionWithAuditEntity.getAuditTimeLastSent(),
            distributionWithAuditEntity.getAuditStatus()
        );
    }
}
