package com.synopsys.integration.alert.database.job.azure.boards;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AzureBoardsJobDetailsRepository extends JpaRepository<AzureBoardsJobDetailsEntity, UUID> {
}
