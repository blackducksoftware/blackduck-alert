/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraServerConfigurationRepository extends JpaRepository<JiraServerConfigurationEntity, UUID> {
    Optional<JiraServerConfigurationEntity> findByName(String name);

    boolean existsByName(String name);
}
