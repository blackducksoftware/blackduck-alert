package com.blackduck.integration.alert.database.configuration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.database.configuration.DescriptorFieldRelation;
import com.blackduck.integration.alert.database.configuration.key.DescriptorFieldRelationPK;

@Component
public interface DescriptorFieldRepository extends JpaRepository<DescriptorFieldRelation, DescriptorFieldRelationPK> {
    List<DescriptorFieldRelation> findByDescriptorId(Long descriptorId);

    List<DescriptorFieldRelation> findByFieldId(Long fieldId);
}
