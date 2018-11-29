package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigsEntity;

@Component
public interface DescriptorConfigsRepository extends JpaRepository<DescriptorConfigsEntity, Long> {
    List<DescriptorConfigsEntity> findByDescriptorId(final Long descriptorId);
}
