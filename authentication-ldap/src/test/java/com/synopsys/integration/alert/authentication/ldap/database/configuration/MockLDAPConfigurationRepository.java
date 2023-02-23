package com.synopsys.integration.alert.authentication.ldap.database.configuration;

import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

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
