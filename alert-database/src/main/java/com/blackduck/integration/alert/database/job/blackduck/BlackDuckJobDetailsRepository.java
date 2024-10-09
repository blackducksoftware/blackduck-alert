package com.blackduck.integration.alert.database.job.blackduck;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackDuckJobDetailsRepository extends JpaRepository<BlackDuckJobDetailsEntity, UUID> {
}
