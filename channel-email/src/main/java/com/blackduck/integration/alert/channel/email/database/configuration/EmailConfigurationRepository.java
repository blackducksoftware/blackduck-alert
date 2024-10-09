package com.blackduck.integration.alert.channel.email.database.configuration;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfigurationRepository extends JpaRepository<EmailConfigurationEntity, UUID> {

    Optional<EmailConfigurationEntity> findByName(String name);

    void deleteByName(String name);

    boolean existsByName(String name);

}
