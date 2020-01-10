/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
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

@Component
@Transactional
public class DefaultDescriptorAccessor implements DescriptorAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final ConfigContextRepository configContextRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;

    @Autowired
    public DefaultDescriptorAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DefinedFieldRepository definedFieldRepository, final ConfigContextRepository configContextRepository,
        final DescriptorTypeRepository descriptorTypeRepository) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.configContextRepository = configContextRepository;
        this.descriptorTypeRepository = descriptorTypeRepository;
    }

    @Override
    public List<RegisteredDescriptorModel> getRegisteredDescriptors() throws AlertDatabaseConstraintException {
        final List<RegisteredDescriptorEntity> allDescriptors = registeredDescriptorRepository.findAll();
        final List<RegisteredDescriptorModel> descriptorModels = new ArrayList<>();
        for (final RegisteredDescriptorEntity entity : allDescriptors) {
            descriptorModels.add(createRegisteredDescriptorModel(entity));
        }

        return descriptorModels;
    }

    @Override
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException {
        if (null == descriptorKey || StringUtils.isBlank(descriptorKey.getUniversalKey())) {
            throw new AlertDatabaseConstraintException(String.format("DescriptorKey is not valid. %s", descriptorKey));
        }

        final Optional<RegisteredDescriptorEntity> descriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey());
        if (descriptorEntity.isPresent()) {
            return Optional.of(createRegisteredDescriptorModel(descriptorEntity.get()));
        }
        return Optional.empty();
    }

    @Override
    // TODO write test for this
    // TODO add foreign key constraint for type on the registered descriptors table
    public List<RegisteredDescriptorModel> getRegisteredDescriptorsByType(final DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        if (null == descriptorType) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be null");
        }

        final Long typeId = saveDescriptorTypeAndReturnId(descriptorType);
        final Collection<RegisteredDescriptorEntity> allDescriptors = registeredDescriptorRepository.findByTypeId(typeId);
        final List<RegisteredDescriptorModel> descriptorModels = new ArrayList<>();
        for (final RegisteredDescriptorEntity entity : allDescriptors) {
            descriptorModels.add(createRegisteredDescriptorModel(entity));
        }

        return descriptorModels;
    }

    @Override
    // TODO write test for this
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(final Long descriptorId) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        return Optional.of(createRegisteredDescriptorModel(descriptor));
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(DescriptorKey descriptorKey, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorByKey(descriptorKey);
        final Long contextId = saveContextAndReturnId(context);
        return getFieldsForDescriptorId(descriptor.getId(), contextId, context);
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        final Long contextId = saveContextAndReturnId(context);
        return getFieldsForDescriptorId(descriptor.getId(), contextId, context);
    }

    private RegisteredDescriptorModel createRegisteredDescriptorModel(final RegisteredDescriptorEntity registeredDescriptorEntity) throws AlertDatabaseConstraintException {
        final Long id = registeredDescriptorEntity.getId();
        final String name = registeredDescriptorEntity.getName();

        final Long typeId = registeredDescriptorEntity.getTypeId();
        final String descriptorType = getDescriptorTypeById(typeId);
        return new RegisteredDescriptorModel(id, name, descriptorType);
    }

    private String getDescriptorTypeById(final Long id) throws AlertDatabaseConstraintException {
        return descriptorTypeRepository
                   .findById(id)
                   .map(DescriptorTypeEntity::getType)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor type with that id exists"));
    }

    private Long saveDescriptorTypeAndReturnId(final DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        if (descriptorType == null) {
            throw new AlertDatabaseConstraintException("The descriptor type cannot be empty");
        }

        final String descriptorTypeString = descriptorType.name();
        final Optional<Long> optionalDescriptorType = descriptorTypeRepository
                                                          .findFirstByType(descriptorTypeString)
                                                          .map(DescriptorTypeEntity::getId);
        if (optionalDescriptorType.isPresent()) {
            return optionalDescriptorType.get();
        }
        final DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity(descriptorTypeString);
        final DescriptorTypeEntity savedDescriptorTypeEntity = descriptorTypeRepository.save(descriptorTypeEntity);
        return savedDescriptorTypeEntity.getId();
    }

    private Long saveContextAndReturnId(final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        if (context == null) {
            throw new AlertDatabaseConstraintException("The context cannot be empty");
        }
        final String contextString = context.name();
        final Optional<Long> optionalConfigContextId = configContextRepository
                                                           .findFirstByContext(contextString)
                                                           .map(ConfigContextEntity::getId);
        if (optionalConfigContextId.isPresent()) {
            return optionalConfigContextId.get();
        }
        final ConfigContextEntity configContextEntity = new ConfigContextEntity(contextString);
        final ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);
        return savedContextEntity.getId();
    }

    private RegisteredDescriptorEntity findDescriptorByKey(DescriptorKey descriptorKey) throws AlertDatabaseConstraintException {
        if (null == descriptorKey || StringUtils.isBlank(descriptorKey.getUniversalKey())) {
            throw new AlertDatabaseConstraintException(String.format("DescriptorKey is not valid. %s", descriptorKey));
        }
        return registeredDescriptorRepository
                   .findFirstByName(descriptorKey.getUniversalKey())
                   .orElseThrow(() -> new AlertDatabaseConstraintException("A descriptor with that name did not exist"));
    }

    private RegisteredDescriptorEntity findDescriptorById(final Long id) throws AlertDatabaseConstraintException {
        if (id == null) {
            throw new AlertDatabaseConstraintException("The descriptor id cannot be null");
        }
        return registeredDescriptorRepository
                   .findById(id)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("A descriptor with that id did not exist"));
    }

    private List<DefinedFieldModel> getFieldsForDescriptorId(final Long descriptorId, final Long contextId, final ConfigContextEnum context) {
        return definedFieldRepository.findByDescriptorIdAndContext(descriptorId, contextId)
                   .stream()
                   .map(entity -> new DefinedFieldModel(entity.getKey(), context, entity.getSensitive()))
                   .collect(Collectors.toList());
    }

}
