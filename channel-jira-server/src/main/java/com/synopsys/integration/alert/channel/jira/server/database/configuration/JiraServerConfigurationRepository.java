package com.synopsys.integration.alert.channel.jira.server.database.configuration;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraServerConfigurationRepository extends JpaRepository<JiraServerConfigurationEntity, UUID> {
}
