package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.ConfigContextEntity;

@Component
public interface ConfigContextRepository extends JpaRepository<ConfigContextEntity, Long> {
    Optional<ConfigContextEntity> findFirstByContext(final String context);

    @Query(value = "SELECT entity FROM ConfigContextEntity entity INNER JOIN entity.fieldContextRelations relation ON entity.id = relation.contextId "
                       + "WHERE relation.fieldId = ?1")
    List<ConfigContextEntity> findByFieldId(Long fieldId);
}
