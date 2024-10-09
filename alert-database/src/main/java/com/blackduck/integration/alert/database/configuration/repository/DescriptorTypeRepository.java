package com.blackduck.integration.alert.database.configuration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackduck.integration.alert.database.configuration.DescriptorTypeEntity;

public interface DescriptorTypeRepository extends JpaRepository<DescriptorTypeEntity, Long> {
    Optional<DescriptorTypeEntity> findFirstByType(String descriptorType);
}
