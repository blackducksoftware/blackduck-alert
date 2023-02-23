package com.synopsys.integration.alert.authentication.saml.database.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SAMLConfigurationRepository extends JpaRepository<SAMLConfigurationEntity, UUID> {
    Optional<SAMLConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
