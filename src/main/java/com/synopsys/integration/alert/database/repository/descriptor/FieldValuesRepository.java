package com.synopsys.integration.alert.database.repository.descriptor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.descriptor.FieldValuesEntity;

@Component
public interface FieldValuesRepository extends JpaRepository<FieldValuesEntity, Long> {
    List<FieldValuesEntity> findByConfigId(final Long configId);
}
