package com.synopsys.integration.alert.database.job.jira.cloud;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraCloudJobDetailsRepository extends JpaRepository<JiraCloudJobDetailsEntity, UUID> {
}
