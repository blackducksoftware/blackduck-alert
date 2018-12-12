/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.api.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.entity.configuration.FieldContextRelation;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.relation.key.DescriptorFieldRelationPK;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.util.Stringable;

@Component
@Transactional
// TODO think about how we can maintain versions of descriptors through code
public class DescriptorAccessor implements BaseDescriptorAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorFieldRepository descriptorFieldRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final FieldContextRepository fieldContextRepository;
    private final ConfigContextRepository configContextRepository;
    private final DescriptorTypeRepository descriptorTypeRepository;

    @Autowired
    public DescriptorAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DescriptorFieldRepository descriptorFieldRepository, final DefinedFieldRepository definedFieldRepository,
        final FieldContextRepository fieldContextRepository, final ConfigContextRepository configContextRepository, final DescriptorTypeRepository descriptorTypeRepository) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorFieldRepository = descriptorFieldRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.fieldContextRepository = fieldContextRepository;
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
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }

        final Optional<RegisteredDescriptorEntity> descriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (descriptorEntity.isPresent()) {
            return Optional.of(createRegisteredDescriptorModel(descriptorEntity.get()));
        }
        return Optional.empty();
    }

    @Override
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
    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorById(final Long descriptorId) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        return Optional.of(createRegisteredDescriptorModel(descriptor));
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    @Override
    public boolean registerDescriptorWithoutFields(final String descriptorName, final DescriptorType descriptorType) throws AlertDatabaseConstraintException {
        return registerDescriptor(descriptorName, descriptorType, Collections.emptyList());
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    @Override
    public boolean registerDescriptor(final String descriptorName, final DescriptorType descriptorType, final Collection<DefinedFieldModel> descriptorFields) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        if (null == descriptorType) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> optionalDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (!optionalDescriptorEntity.isPresent()) {
            final Long descriptorTypeId = saveDescriptorTypeAndReturnId(descriptorType);
            final RegisteredDescriptorEntity newDescriptor = new RegisteredDescriptorEntity(descriptorName, descriptorTypeId);
            final RegisteredDescriptorEntity createdDescriptor = registeredDescriptorRepository.save(newDescriptor);
            addFieldsAndUpdateRelations(createdDescriptor.getId(), descriptorFields);
            return true;
        }
        return false;
    }

    /**
     * @return true if the descriptor with that name was present
     */
    @Override
    public boolean unregisterDescriptor(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> existingDescriptor = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (existingDescriptor.isPresent()) {
            registeredDescriptorRepository.delete(existingDescriptor.get());
            return true;
        }
        return false;
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptor(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorByName(descriptorName);
        return getFieldsForDescriptorId(descriptor.getId(), context);
    }

    @Override
    public List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        return getFieldsForDescriptorId(descriptor.getId(), context);
    }

    @Override
    public DefinedFieldModel addDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException {
        // The following method will verify the descriptor exists, otherwise throw an exception:
        findDescriptorById(descriptorId);
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("The descriptor field cannot be null");
        }
        addFieldsAndUpdateRelations(descriptorId, Arrays.asList(descriptorField));
        return descriptorField;
    }

    public DefinedFieldModel updateDefinedFieldKey(final String oldKey, final String newKey) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(oldKey)) {
            throw new AlertDatabaseConstraintException("The old field key cannot be empty");
        }
        if (StringUtils.isEmpty(newKey)) {
            throw new AlertDatabaseConstraintException("The new field key cannot be empty");
        }
        final DefinedFieldEntity foundDefinedFieldEntity = findFieldByKey(oldKey);
        final DefinedFieldEntity newDefinedFieldEntity = new DefinedFieldEntity(newKey, foundDefinedFieldEntity.getSensitive());
        newDefinedFieldEntity.setId(foundDefinedFieldEntity.getId());
        final DefinedFieldEntity savedDefinedFieldEntity = definedFieldRepository.save(newDefinedFieldEntity);

        final Set<ConfigContextEnum> contextEnums = getContextsForFieldId(savedDefinedFieldEntity.getId());
        return new DefinedFieldModel(savedDefinedFieldEntity.getKey(), contextEnums, savedDefinedFieldEntity.getSensitive());
    }

    /**
     * @return true if the descriptor field was present
     */
    public boolean removeDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("The descriptor field cannot be null");
        }

        if (StringUtils.isEmpty(descriptorField.getKey())) {
            throw new AlertDatabaseConstraintException("The field key cannot be empty");
        }
        final DefinedFieldEntity definedFieldEntity;
        try {
            definedFieldEntity = findFieldByKey(descriptorField.getKey());
        } catch (final AlertDatabaseConstraintException e) {
            return false;
        }
        final Long fieldId = definedFieldEntity.getId();
        final DescriptorFieldRelationPK relationPK = new DescriptorFieldRelationPK(descriptor.getId(), fieldId);
        final Optional<DescriptorFieldRelation> optionalDescriptorFieldRelation = descriptorFieldRepository.findById(relationPK);
        if (optionalDescriptorFieldRelation.isPresent()) {
            final DescriptorFieldRelation descriptorFieldRelation = optionalDescriptorFieldRelation.get();
            descriptorFieldRepository.delete(descriptorFieldRelation);
            final boolean fieldIsNotConfiguredForAnyDescriptors = descriptorFieldRepository
                                                                      .findByFieldId(fieldId)
                                                                      .isEmpty();
            if (fieldIsNotConfiguredForAnyDescriptors) {
                definedFieldRepository.deleteById(fieldId);
            }
            return true;
        }
        return false;
    }

    /**
     * @return true if the descriptor field was present
     */
    public boolean deleteDefinedField(final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException {
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("Cannot delete a null object from the database");
        }
        return deleteDefinedField(descriptorField.getKey());
    }

    /**
     * @return true if a field with that key was present
     */
    public boolean deleteDefinedField(final String fieldKey) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(fieldKey)) {
            throw new AlertDatabaseConstraintException("The field key cannot be empty");
        }
        try {
            final DefinedFieldEntity definedFieldEntity = findFieldByKey(fieldKey);
            definedFieldRepository.delete(definedFieldEntity);
            return true;
        } catch (final AlertDatabaseConstraintException e) {
            return false;
        }
    }

    private RegisteredDescriptorModel createRegisteredDescriptorModel(final RegisteredDescriptorEntity registeredDescriptorEntity) throws AlertDatabaseConstraintException {
        final Long id = registeredDescriptorEntity.getId();
        final String name = registeredDescriptorEntity.getName();

        final Long typeId = registeredDescriptorEntity.getTypeId();
        final String descriptorType = getDescriptorTypeById(typeId);
        return new RegisteredDescriptorModel(id, name, descriptorType);
    }

    private void addFieldsAndUpdateRelations(final Long descriptorId, final Collection<DefinedFieldModel> fieldsToAdd) throws AlertDatabaseConstraintException {
        for (final DefinedFieldModel fieldModel : fieldsToAdd) {
            final Long fieldId = saveFieldAndReturnId(fieldModel);
            saveDescriptorFieldRelationIfNecessary(descriptorId, fieldId);
            for (final ConfigContextEnum context : fieldModel.getContexts()) {
                final Long contextId = saveContextAndReturnId(context);
                saveFieldContextRelationIfNecessary(fieldId, contextId);
            }
        }
    }

    private Long saveFieldAndReturnId(final DefinedFieldModel fieldModel) throws AlertDatabaseConstraintException {
        final String key = fieldModel.getKey();
        if (StringUtils.isEmpty(key)) {
            throw new AlertDatabaseConstraintException("The field key cannot be empty");
        }
        final Optional<Long> optionalDefinedFieldId = definedFieldRepository
                                                          .findFirstByKey(key)
                                                          .map(DefinedFieldEntity::getId);
        if (optionalDefinedFieldId.isPresent()) {
            return optionalDefinedFieldId.get();
        }
        final DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(key, fieldModel.getSensitive());
        final DefinedFieldEntity savedEntity = definedFieldRepository.save(definedFieldEntity);
        return savedEntity.getId();
    }

    private void saveDescriptorFieldRelationIfNecessary(final Long descriptorId, final Long fieldId) {
        final boolean alreadySaved = descriptorFieldRepository
                                         .findByFieldId(fieldId)
                                         .stream()
                                         .anyMatch(relation -> descriptorId.equals(relation.getDescriptorId()));
        if (!alreadySaved) {
            final DescriptorFieldRelation relation = new DescriptorFieldRelation(descriptorId, fieldId);
            descriptorFieldRepository.save(relation);
        }
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

    private void saveFieldContextRelationIfNecessary(final Long fieldId, final Long contextId) {
        final boolean alreadySaved = fieldContextRepository
                                         .findByContextId(contextId)
                                         .stream()
                                         .anyMatch(relation -> fieldId.equals(relation.getFieldId()));
        if (!alreadySaved) {
            final FieldContextRelation relation = new FieldContextRelation(fieldId, contextId);
            fieldContextRepository.save(relation);
        }
    }

    private RegisteredDescriptorEntity findDescriptorByName(final String name) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(name)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorRepository
                   .findFirstByName(name)
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

    // TODO implement a join
    private List<DefinedFieldModel> getFieldsForDescriptorId(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final Set<Long> fieldIds = descriptorFieldRepository
                                       .findByDescriptorId(descriptorId)
                                       .stream()
                                       .map(DescriptorFieldRelation::getFieldId)
                                       .collect(Collectors.toSet());
        if (fieldIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Long contextId = saveContextAndReturnId(context);
        fieldContextRepository
            .findByContextId(contextId)
            .stream()
            .filter(relation -> fieldIds.contains(relation.getFieldId()))
            .map(FieldContextRelation::getFieldId)
            .collect(Collectors.toList());

        return definedFieldRepository
                   .findAllById(fieldIds)
                   .stream()
                   .map(entity -> new DefinedFieldModel(entity.getKey(), context, entity.getSensitive()))
                   .collect(Collectors.toList());
    }

    private Set<ConfigContextEnum> getContextsForFieldId(final Long fieldId) {
        return fieldContextRepository
                   .findByFieldId(fieldId)
                   .stream()
                   .map(FieldContextRelation::getContextId)
                   .flatMap(configId -> configContextRepository.findAllById(Collections.singleton(configId)).stream())
                   .map(entity -> ConfigContextEnum.valueOf(entity.getContext()))
                   .collect(Collectors.toSet());
    }

    private DefinedFieldEntity findFieldByKey(final String key) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(key)) {
            throw new AlertDatabaseConstraintException("The field key cannot be empty");
        }
        return definedFieldRepository
                   .findFirstByKey(key)
                   .orElseThrow(() -> new AlertDatabaseConstraintException("No field with that key exists"));
    }

    public final class RegisteredDescriptorModel extends Stringable {
        private final Long id;
        private final String name;
        private final DescriptorType type;

        private RegisteredDescriptorModel(final Long registeredDescriptorId, final String registeredDescriptorName, final String registeredDescriptorType) {
            this(registeredDescriptorId, registeredDescriptorName, DescriptorType.valueOf(registeredDescriptorType));
        }

        private RegisteredDescriptorModel(final Long registeredDescriptorId, final String registeredDescriptorName, final DescriptorType registeredDescriptorType) {
            id = registeredDescriptorId;
            name = registeredDescriptorName;
            type = registeredDescriptorType;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public DescriptorType getType() {
            return type;
        }
    }
}
