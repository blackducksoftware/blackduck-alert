package com.synopsys.integration.alert.database.provider.task;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTaskPropertiesRepository extends JpaRepository<ProviderTaskPropertiesEntity, Long> {
    Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName);

}
