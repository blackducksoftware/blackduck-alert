package com.synopsys.integration.alert.api.oauth.database.configuration;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertOAuthConfigurationRepository extends JpaRepository<AlertOAuthConfigurationEntity, UUID> {
}
