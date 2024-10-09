package com.blackduck.integration.alert.channel.jira.server.database.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraServerJobDetailsRepository extends JpaRepository<JiraServerJobDetailsEntity, UUID> {
}
