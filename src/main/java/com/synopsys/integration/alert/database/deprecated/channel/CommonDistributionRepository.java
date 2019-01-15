package com.synopsys.integration.alert.database.deprecated.channel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonDistributionRepository extends JpaRepository<CommonDistributionConfigEntity, Long> {
    CommonDistributionConfigEntity findByDistributionConfigIdAndDistributionType(final Long distributionConfigId, final String distributionType);

    CommonDistributionConfigEntity findByName(String name);
}
