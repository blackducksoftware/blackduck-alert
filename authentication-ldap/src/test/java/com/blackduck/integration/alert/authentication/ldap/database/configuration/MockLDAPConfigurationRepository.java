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

import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockLDAPConfigurationRepository extends MockRepositoryContainer<UUID, LDAPConfigurationEntity> implements LDAPConfigurationRepository {
    public MockLDAPConfigurationRepository() {
        super(LDAPConfigurationEntity::getConfigurationId);
    }

    @Override
    public Optional<LDAPConfigurationEntity> findByName(String name) {
        return findAll()
            .stream()
            .filter(entity -> entity.getName().equals(name))
            .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return findAll()
            .stream()
            .anyMatch(entity -> entity.getName().equals(name));
    }

    @Override
    public void deleteByName(String name) {
        findByName(name)
            .map(LDAPConfigurationEntity::getConfigurationId)
            .ifPresent(this::deleteById);
    }
}
