package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionJobRepository extends JpaRepository<DistributionJobEntity, UUID> {
    Optional<DistributionJobEntity> findByName(String name);

    List<DistributionJobEntity> findByDistributionFrequency(String distributionFrequency);

    Page<DistributionJobEntity> findByChannelDescriptorNameIn(Collection<String> channelDescriptorName, Pageable pageable);

}
