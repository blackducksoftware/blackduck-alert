package com.synopsys.integration.alert.database.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionJobRepository extends JpaRepository<DistributionJobEntity, UUID> {
}
