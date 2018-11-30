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
package com.synopsys.integration.alert.database.api.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldsEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorsEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldsRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorsRepository;
import com.synopsys.integration.util.Stringable;

@Component
@Transactional
// TODO think about how we can version descriptors through code
public class DescriptorAccessor {
    private final RegisteredDescriptorsRepository registeredDescriptorsRepository;
    private final DescriptorFieldsRepository descriptorFieldsRepository;

    @Autowired
    public DescriptorAccessor(final RegisteredDescriptorsRepository registeredDescriptorsRepository, final DescriptorFieldsRepository descriptorFieldsRepository) {
        this.registeredDescriptorsRepository = registeredDescriptorsRepository;
        this.descriptorFieldsRepository = descriptorFieldsRepository;
    }

    public List<RegisteredDescriptorModel> getRegisteredDescriptors() {
        return registeredDescriptorsRepository
                       .findAll()
                       .stream()
                       .map(RegisteredDescriptorModel::new)
                       .collect(Collectors.toList());
    }

    public Optional<RegisteredDescriptorModel> getRegisteredDescriptorByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorsRepository
                       .findFirstByName(descriptorName)
                       .map(RegisteredDescriptorModel::new);
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    public boolean registerDescriptorWithoutFields(final String descriptorName, final String descriptorType) throws AlertDatabaseConstraintException {
        return registerDescriptor(descriptorName, descriptorType, Collections.emptyList());
    }

    /**
     * @return true if a descriptor with that name was not already registered
     */
    public boolean registerDescriptor(final String descriptorName, final String descriptorType, final Collection<DescriptorFieldModel> descriptorFields) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorsEntity> optionalDescriptorsEntity = registeredDescriptorsRepository.findFirstByName(descriptorName);
        if (!optionalDescriptorsEntity.isPresent()) {
            final RegisteredDescriptorsEntity newDescriptor = new RegisteredDescriptorsEntity(descriptorName, descriptorType);
            final RegisteredDescriptorsEntity createdDescriptor = registeredDescriptorsRepository.save(newDescriptor);
            for (final DescriptorFieldModel field : descriptorFields) {
                final DescriptorFieldsEntity newFieldEntity = new DescriptorFieldsEntity(createdDescriptor.getId(), field.getKey(), field.getSensitive());
                descriptorFieldsRepository.save(newFieldEntity);
            }
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
        final Optional<RegisteredDescriptorsEntity> existingDescriptor = registeredDescriptorsRepository.findFirstByName(descriptorName);
        if (existingDescriptor.isPresent()) {
            registeredDescriptorsRepository.delete(existingDescriptor.get());
            return true;
        }
        return false;
    }

    public List<DescriptorFieldModel> getFieldsForDescriptorId(final Long descriptorId) throws AlertDatabaseConstraintException {
        findDescriptorById(descriptorId);
        return descriptorFieldsRepository
                       .findByDescriptorId(descriptorId)
                       .stream()
                       .map(entity -> new DescriptorFieldModel(entity.getKey(), entity.getSensitive()))
                       .collect(Collectors.toList());
    }

    public DescriptorFieldModel addDescriptorField(final Long descriptorId, final DescriptorFieldModel descriptorField) throws AlertDatabaseConstraintException {
        // The following method will verify the descriptor exists, otherwise throw an exception:
        findDescriptorById(descriptorId);
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("The descriptor field cannot be null");
        }
        final DescriptorFieldsEntity newDescriptorFieldsEntity = new DescriptorFieldsEntity(descriptorId, descriptorField.getKey(), descriptorField.getSensitive());
        final DescriptorFieldsEntity createdDescriptorFieldsEntity = descriptorFieldsRepository.save(newDescriptorFieldsEntity);
        return new DescriptorFieldModel(createdDescriptorFieldsEntity.getKey(), createdDescriptorFieldsEntity.getSensitive());
    }

    public DescriptorFieldModel updateDescriptorField(final Long descriptorId, final DescriptorFieldModel descriptorField) throws AlertDatabaseConstraintException {
        // The following method will verify the descriptor exists, otherwise throw an exception:
        findDescriptorById(descriptorId);
        if (descriptorField == null) {
            throw new AlertDatabaseConstraintException("The descriptor field cannot be null");
        }
        final DescriptorFieldsEntity foundDescriptorFieldsEntity = findFieldByKey(descriptorId, descriptorField.getKey());
        final DescriptorFieldsEntity descriptorFieldsEntityToUpdate = new DescriptorFieldsEntity(descriptorId, descriptorField.getKey(), descriptorField.getSensitive());
        descriptorFieldsEntityToUpdate.setId(foundDescriptorFieldsEntity.getId());
        final DescriptorFieldsEntity savedDescriptorFieldsEntity = descriptorFieldsRepository.save(descriptorFieldsEntityToUpdate);
        return new DescriptorFieldModel(savedDescriptorFieldsEntity.getKey(), savedDescriptorFieldsEntity.getSensitive());
    }

    /**
     * @return true if the descriptor field was present
     */
    public boolean deleteDescriptorField(final Long descriptorId, final DescriptorFieldModel descriptorField) throws AlertDatabaseConstraintException {
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
            final DescriptorFieldsEntity descriptorFieldsEntity = findFieldByKey(descriptorId, fieldKey);
            descriptorFieldsRepository.delete(descriptorFieldsEntity);
            return true;
        } catch (final AlertDatabaseConstraintException e) {
            return false;
        }
    }

    private RegisteredDescriptorsEntity findDescriptorById(final Long id) throws AlertDatabaseConstraintException {
        if (id == null) {
            throw new AlertDatabaseConstraintException("The descriptor id cannot be null");
        }
        return registeredDescriptorsRepository
                       .findById(id)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("A descriptor with that id did not exist"));
    }

    private DescriptorFieldsEntity findFieldByKey(final Long descriptorId, final String key) throws AlertDatabaseConstraintException {
        if (descriptorId == null) {
            throw new AlertDatabaseConstraintException("The descriptor id cannot be null");
        }
        if (StringUtils.isEmpty(key)) {
            throw new AlertDatabaseConstraintException("The field key cannot be empty");
        }
        return descriptorFieldsRepository
                       .findFirstByDescriptorIdAndKey(descriptorId, key)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("No field for that descriptor with that key exists"));
    }

    public class RegisteredDescriptorModel extends Stringable {
        private final Long id;
        private final String name;
        private final String type;

        private RegisteredDescriptorModel(final RegisteredDescriptorsEntity registeredDescriptorsEntity) {
            this.id = registeredDescriptorsEntity.getId();
            this.name = registeredDescriptorsEntity.getName();
            this.type = registeredDescriptorsEntity.getType();
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
