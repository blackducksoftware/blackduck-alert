package com.synopsys.integration.alert.authentication.saml.database.mock;

import com.synopsys.integration.alert.authentication.saml.database.configuration.SAMLConfigurationEntity;
import com.synopsys.integration.alert.authentication.saml.database.configuration.SAMLConfigurationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

import java.util.Optional;
import java.util.UUID;

public class MockSAMLConfigurationRepository extends MockRepositoryContainer<UUID, SAMLConfigurationEntity> implements SAMLConfigurationRepository {
    public MockSAMLConfigurationRepository() {
        super(SAMLConfigurationEntity::getConfigurationId);
    }

    @Override
    public Optional<SAMLConfigurationEntity> findByName(String name) {
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
            .map(SAMLConfigurationEntity::getConfigurationId)
            .ifPresent(this::deleteById);
    }
}
