package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorsEntity;

@Component
public interface RegisteredDescriptorsRepository extends JpaRepository<RegisteredDescriptorsEntity, Long> {
    Optional<RegisteredDescriptorsEntity> findFirstByName(final String descriptorName);
    List<RegisteredDescriptorsEntity> findByType(final String descriptorType);
}
