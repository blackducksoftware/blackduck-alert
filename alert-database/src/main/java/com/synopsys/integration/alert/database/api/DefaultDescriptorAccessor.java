/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
@Transactional
public class DefaultDescriptorAccessor implements DescriptorAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final ConfigContextRepository configContextRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;

    @Autowired
    public DefaultDescriptorAccessor(RegisteredDescriptorRepository registeredDescriptorRepository, DefinedFieldRepository definedFieldRepository, ConfigContextRepository configContextRepository,
        DescriptorTypeRepository descriptorTypeRepository) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.configContextRepository = configContextRepository;
        this.descriptorTypeRepository = descriptorTypeRepository;
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptors() {
        List<RegisteredDescriptorEntity> allDescriptors = registeredDescriptorRepository.findAll();
        List<RegisteredDescriptorModel> descriptorModels = new ArrayList<>();
        for (RegisteredDescriptorEntity entity : allDescriptors) {
            descriptorModels.add(createRegisteredDescriptorModel(entity));
        }

        return descriptorModels;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey) {
        return registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())
                   .map(this::createRegisteredDescriptorModel);
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(DescriptorType descriptorType) {
        Long typeId = saveDescriptorTypeAndReturnId(descriptorType);
        Collection<RegisteredDescriptorEntity> allDescriptors = registeredDescriptorRepository.findByTypeId(typeId);
        List<RegisteredDescriptorModel> descriptorModels = new ArrayList<>();
        for (RegisteredDescriptorEntity entity : allDescriptors) {
            descriptorModels.add(createRegisteredDescriptorModel(entity));
        }

        return descriptorModels;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(Long descriptorId) {
        RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        return Optional.of(createRegisteredDescriptorModel(descriptor));
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, ConfigContextEnum context) {
        RegisteredDescriptorEntity descriptor = findDescriptorByKey(descriptorKey);
        Long contextId = saveContextAndReturnId(context);
        return getFieldsForDescriptorId(descriptor.getId(), contextId, context);
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(Long descriptorId, ConfigContextEnum context) {
        RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        Long contextId = saveContextAndReturnId(context);
        return getFieldsForDescriptorId(descriptor.getId(), contextId, context);
    }

    private RegisteredDescriptorModel createRegisteredDescriptorModel(RegisteredDescriptorEntity registeredDescriptorEntity) {
        Long id = registeredDescriptorEntity.getId();
        String name = registeredDescriptorEntity.getName();

        Long typeId = registeredDescriptorEntity.getTypeId();
        String descriptorType = getDescriptorTypeById(typeId);
        return new RegisteredDescriptorModel(id, name, descriptorType);
    }

    private String getDescriptorTypeById(Long id) {
        return descriptorTypeRepository
                   .findById(id)
                   .map(DescriptorTypeEntity::getType)
                   .orElseThrow(() -> new AlertRuntimeException("No descriptor type with that id exists"));
    }

    private Long saveDescriptorTypeAndReturnId(DescriptorType descriptorType) {
        String descriptorTypeString = descriptorType.name();
        Optional<Long> optionalDescriptorType = descriptorTypeRepository
                                                    .findFirstByType(descriptorTypeString)
                                                    .map(DescriptorTypeEntity::getId);
        if (optionalDescriptorType.isPresent()) {
            return optionalDescriptorType.get();
        }
        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorTypeString);
        DescriptorTypeEntity savedDescriptorTypeEntity = descriptorTypeRepository.save(descriptorTypeEntity);
        return savedDescriptorTypeEntity.getId();
    }

    private Long saveContextAndReturnId(ConfigContextEnum context) {
        String contextString = context.name();
        Optional<Long> optionalConfigContextId = configContextRepository
                                                     .findFirstByContext(contextString)
                                                     .map(ConfigContextEntity::getId);
        if (optionalConfigContextId.isPresent()) {
            return optionalConfigContextId.get();
        }
        ConfigContextEntity configContextEntity = new ConfigContextEntity(contextString);
        ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);
        return savedContextEntity.getId();
    }

    private RegisteredDescriptorEntity findDescriptorByKey(DescriptorKey descriptorKey) {
        return registeredDescriptorRepository
                   .findFirstByName(descriptorKey.getUniversalKey())
                   .orElseThrow(() -> new AlertRuntimeException("A descriptor with that name did not exist"));
    }

    private RegisteredDescriptorEntity findDescriptorById(Long id) {
        return registeredDescriptorRepository
                   .findById(id)
                   .orElseThrow(() -> new AlertRuntimeException("A descriptor with that id did not exist"));
    }

    private List<DefinedFieldModel> getFieldsForDescriptorId(Long descriptorId, Long contextId, ConfigContextEnum context) {
        return definedFieldRepository.findByDescriptorIdAndContext(descriptorId, contextId)
                   .stream()
                   .map(entity -> new DefinedFieldModel(entity.getKey(), context, entity.getSensitive()))
                   .collect(Collectors.toList());
    }

}
