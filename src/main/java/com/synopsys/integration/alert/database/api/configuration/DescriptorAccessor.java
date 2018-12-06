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

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.entity.descriptor.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.entity.descriptor.FieldContextRelation;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.util.Stringable;

@Component
@Transactional
// TODO think about how we can maintain versions of descriptors through code
// TODO re-add type
public class DescriptorAccessor {
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorFieldRepository descriptorFieldRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final FieldContextRepository fieldContextRepository;
    private final ConfigContextRepository configContextRepository;

    @Autowired
    public DescriptorAccessor(final RegisteredDescriptorRepository registeredDescriptorRepository, final DescriptorFieldRepository descriptorFieldRepository, final DefinedFieldRepository definedFieldRepository,
            final FieldContextRepository fieldContextRepository, final ConfigContextRepository configContextRepository) {
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorFieldRepository = descriptorFieldRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.fieldContextRepository = fieldContextRepository;
        this.configContextRepository = configContextRepository;
    }

    public List<RegisteredDescriptorModel> getRegisteredDescriptors() {
        return registeredDescriptorRepository
                       .findAll()
                       .stream()
                       .map(RegisteredDescriptorModel::new)
                       .collect(Collectors.toList());
    }

    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorRepository
                       .findFirstByName(descriptorName)
                       .map(RegisteredDescriptorModel::new);
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    public boolean registerDescriptorWithoutFields(final String descriptorName) throws AlertDatabaseConstraintException {
        return registerDescriptor(descriptorName, Collections.emptyList());
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    public boolean registerDescriptor(final String descriptorName, final Collection<DefinedFieldModel> descriptorFields) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorEntity> optionalDescriptorEntity = registeredDescriptorRepository.findFirstByName(descriptorName);
        if (!optionalDescriptorEntity.isPresent()) {
            final RegisteredDescriptorEntity newDescriptor = new RegisteredDescriptorEntity(descriptorName);
            final RegisteredDescriptorEntity createdDescriptor = registeredDescriptorRepository.save(newDescriptor);
            addFieldsAndUpdateRelations(createdDescriptor.getId(), descriptorFields);
            return true;
        }
        return false;
    }

    /**
     * @return true if the descriptor with that name was present
     */
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

    public List<DefinedFieldModel> getFieldsForDescriptor(final String descriptorName, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorByName(descriptorName);
        return getFieldsForDescriptorId(descriptor.getId(), context);
    }

    public List<DefinedFieldModel> getFieldsForDescriptorById(final Long descriptorId, final ConfigContextEnum context) throws AlertDatabaseConstraintException {
        final RegisteredDescriptorEntity descriptor = findDescriptorById(descriptorId);
        return getFieldsForDescriptorId(descriptor.getId(), context);
    }

    public DefinedFieldModel addDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException {
        // The following method will verify the descriptor exists, otherwise throw an exception:
        findDescriptorById(descriptorId);
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("The descriptor field cannot be null");
        }
        addFieldsAndUpdateRelations(descriptorId, Arrays.asList(descriptorField));
        return descriptorField;
    }

    public DefinedFieldModel updateFieldKey(final String oldKey, final String newKey) throws AlertDatabaseConstraintException {
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
    public boolean deleteDescriptorField(final Long descriptorId, final DefinedFieldModel descriptorField) throws AlertDatabaseConstraintException {
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("Cannot delete a null object from the database");
        }
        return deleteDescriptorField(descriptorId, descriptorField.getKey());
    }

    /**
     * @return true if a field with that key was present
     */
    public boolean deleteDescriptorField(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException {
        if (descriptorId == null) {
            throw new AlertDatabaseConstraintException("The descriptor id cannot be null");
        }
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

        private RegisteredDescriptorModel(final RegisteredDescriptorEntity registeredDescriptorsEntity) {
            this.id = registeredDescriptorsEntity.getId();
            this.name = registeredDescriptorsEntity.getName();
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
