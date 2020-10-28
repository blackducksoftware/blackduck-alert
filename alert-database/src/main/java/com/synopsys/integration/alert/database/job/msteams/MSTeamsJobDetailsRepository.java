package com.synopsys.integration.alert.database.job.msteams;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MSTeamsJobDetailsRepository extends JpaRepository<MSTeamsJobDetailsEntity, UUID> {
}
