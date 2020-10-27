package com.synopsys.integration.alert.database.job.jira.server;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraServerJobDetailsRepository extends JpaRepository<JiraServerJobDetailsEntity, UUID> {
}
