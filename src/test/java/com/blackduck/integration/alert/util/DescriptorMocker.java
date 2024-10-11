/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.persistence.model.DefinedFieldModel;
import com.blackduck.integration.alert.database.DatabaseEntity;
import com.blackduck.integration.alert.database.configuration.ConfigContextEntity;
import com.blackduck.integration.alert.database.configuration.DefinedFieldEntity;
import com.blackduck.integration.alert.database.configuration.DescriptorFieldRelation;
import com.blackduck.integration.alert.database.configuration.DescriptorTypeEntity;
import com.blackduck.integration.alert.database.configuration.FieldContextRelation;
import com.blackduck.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.blackduck.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.blackduck.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.blackduck.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.blackduck.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.blackduck.integration.alert.database.configuration.repository.FieldContextRepository;
import com.blackduck.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

@Transactional
public class DescriptorMocker {
    private final DescriptorTypeRepository descriptorTypeRepository;
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorFieldRepository descriptorFieldRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final FieldContextRepository fieldContextRepository;
    private final ConfigContextRepository configContextRepository;

    public DescriptorMocker(DescriptorTypeRepository descriptorTypeRepository, RegisteredDescriptorRepository registeredDescriptorRepository,
        DescriptorFieldRepository descriptorFieldRepository, DefinedFieldRepository definedFieldRepository, FieldContextRepository fieldContextRepository,
        ConfigContextRepository configContextRepository) {
        this.descriptorTypeRepository = descriptorTypeRepository;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorFieldRepository = descriptorFieldRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.fieldContextRepository = fieldContextRepository;
        this.configContextRepository = configContextRepository;
    }

    public void registerDescriptor(String descriptorName, DescriptorType type, Collection<DefinedFieldModel> fields) {
        registerDescriptor(descriptorName, type);
        for (DefinedFieldModel field : fields) {
            addFieldToDescriptor(descriptorName, field);
        }
    }

    public void registerDescriptor(String descriptorName, DescriptorType type) {
        Optional<Long> optionalTypeId = descriptorTypeRepository.findFirstByType(type.name()).map(DatabaseEntity::getId);
        Long typeId;
        if (optionalTypeId.isPresent()) {
            typeId = optionalTypeId.get();
        } else {
            typeId = descriptorTypeRepository.save(new DescriptorTypeEntity(type.name())).getId();
        }
        registeredDescriptorRepository.save(new RegisteredDescriptorEntity(descriptorName, typeId)).getId();
    }

    public void unregisterDescriptor(String name) {
        registeredDescriptorRepository.findFirstByName(name).ifPresent(registeredDescriptorRepository::delete);
    }

    public void addFieldToDescriptor(String descriptorName, DefinedFieldModel definedFieldModel) {
        addFieldToDescriptor(descriptorName, definedFieldModel.getKey(), definedFieldModel.getContexts().stream().findFirst().orElse(ConfigContextEnum.GLOBAL), definedFieldModel.getSensitive());
    }

    public void addFieldToDescriptor(String descriptorName, String fieldKey, ConfigContextEnum context, Boolean sensitive) {
        addFieldToDescriptor(descriptorName, fieldKey, Set.of(context), sensitive);
    }

    public void addFieldToDescriptor(String descriptorName, String fieldKey, Collection<ConfigContextEnum> contexts, Boolean sensitive) {
        Long descriptorId = registeredDescriptorRepository.findFirstByName(descriptorName).map(RegisteredDescriptorEntity::getId).orElseThrow();
        Long fieldId = definedFieldRepository.findFirstByKey(fieldKey).map(DefinedFieldEntity::getId).orElseGet(() -> definedFieldRepository.save(new DefinedFieldEntity(fieldKey, sensitive)).getId());
        try {
            descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorId, fieldId));
        } catch (Exception e) {
            // Ignore exception, relation already registered
        }
        for (ConfigContextEnum context : contexts) {
            String contextName = context.name();
            Long contextId = configContextRepository.findFirstByContext(contextName).map(ConfigContextEntity::getId).orElseGet(() -> configContextRepository.save(new ConfigContextEntity(contextName)).getId());
            try {
                fieldContextRepository.save(new FieldContextRelation(fieldId, contextId));
            } catch (Exception e) {
                // Ignore exception, relation already registered
            }
        }
    }

    public void cleanUpDescriptors() {
        registeredDescriptorRepository.flush();
        definedFieldRepository.flush();
        configContextRepository.flush();
        registeredDescriptorRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        // No need to delete relations as they will be deleted by the tables they reference (CASCADE)
    }
}
