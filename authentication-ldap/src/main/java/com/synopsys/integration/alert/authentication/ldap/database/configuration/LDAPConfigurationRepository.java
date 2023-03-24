package com.synopsys.integration.alert.authentication.ldap.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LDAPConfigurationRepository extends JpaRepository<LDAPConfigurationEntity, UUID> {
    Optional<LDAPConfigurationEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
