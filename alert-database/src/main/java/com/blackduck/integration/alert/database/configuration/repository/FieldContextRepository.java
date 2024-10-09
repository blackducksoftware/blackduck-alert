package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.FieldContextRelation;
import com.blackduck.integration.alert.database.configuration.key.FieldContextRelationPK;

@Component
public interface FieldContextRepository extends JpaRepository<FieldContextRelation, FieldContextRelationPK> {
    List<FieldContextRelation> findByFieldId(Long fieldId);

    List<FieldContextRelation> findByContextId(Long contextId);
}
