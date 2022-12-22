package com.synopsys.integration.alert.authentication.saml.database.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationSAMLConfigurationRepository extends JpaRepository<AuthenticationSAMLConfigurationEntity, UUID> {
    Optional<AuthenticationSAMLConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
