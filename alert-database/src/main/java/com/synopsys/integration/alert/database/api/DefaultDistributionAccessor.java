/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
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
    public DistributionWithAuditInfo getDistributionWithAuditInfo() {
        DistributionWithAuditEntity distributionWithAuditInfo = distributionRepository.getDistributionWithAuditInfo();
        return convert(distributionWithAuditInfo);
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
