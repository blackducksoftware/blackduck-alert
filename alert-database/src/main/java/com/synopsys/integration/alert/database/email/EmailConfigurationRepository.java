package com.synopsys.integration.alert.database.email;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfigurationRepository extends JpaRepository<EmailConfigurationEntity, UUID> {

}
