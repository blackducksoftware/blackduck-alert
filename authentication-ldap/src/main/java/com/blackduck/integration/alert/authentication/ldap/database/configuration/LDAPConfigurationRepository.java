/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LDAPConfigurationRepository extends JpaRepository<LDAPConfigurationEntity, UUID> {
    Optional<LDAPConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
