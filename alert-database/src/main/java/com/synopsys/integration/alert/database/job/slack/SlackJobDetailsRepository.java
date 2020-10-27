package com.synopsys.integration.alert.database.job.slack;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackJobDetailsRepository extends JpaRepository<SlackJobDetailsEntity, UUID> {
}
