package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.ConfigContextEntity;

@Component
public interface ConfigContextRepository extends JpaRepository<ConfigContextEntity, Long> {
    Optional<ConfigContextEntity> findFirstByContext(final String context);
}
