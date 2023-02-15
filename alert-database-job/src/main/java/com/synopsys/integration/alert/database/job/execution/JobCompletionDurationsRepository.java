package com.synopsys.integration.alert.database.job.execution;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobCompletionDurationsRepository extends JpaRepository<JobCompletionStatusDurationsEntity, UUID> {
}
