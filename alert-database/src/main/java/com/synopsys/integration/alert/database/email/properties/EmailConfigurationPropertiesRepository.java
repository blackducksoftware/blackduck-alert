package com.synopsys.integration.alert.database.email.properties;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfigurationPropertiesRepository extends JpaRepository<EmailConfigurationsProperties, EmailConfigurationPropertiesPK> {
}
