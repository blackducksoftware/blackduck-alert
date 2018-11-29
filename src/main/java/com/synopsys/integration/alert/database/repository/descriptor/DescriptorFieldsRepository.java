package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldsEntity;

@Component
public interface DescriptorFieldsRepository extends JpaRepository<DescriptorFieldsEntity, Long> {
    Optional<DescriptorFieldsEntity> findFirstByDescriptorIdAndKey(final Long descriptorId, final String fieldKey);
}
