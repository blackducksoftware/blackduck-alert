package com.synopsys.integration.alert.database.api.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigsEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldsEntity;
import com.synopsys.integration.alert.database.entity.descriptor.FieldValuesEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorsEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigsRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldsRepository;
import com.synopsys.integration.alert.database.repository.descriptor.FieldValuesRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorsRepository;

@Component
@Transactional
// TODO split this into descriptor accessor and config accessor
// FIXME make sure encryption / decryption is implemented
public class RegisteredDescriptorAccessor {
    private final RegisteredDescriptorsRepository registeredDescriptorsRepository;
    private final DescriptorFieldsRepository descriptorFieldsRepository;
    private final DescriptorConfigsRepository descriptorConfigsRepository;
    private final FieldValuesRepository fieldValuesRepository;

    @Autowired
    public RegisteredDescriptorAccessor(final RegisteredDescriptorsRepository registeredDescriptorsRepository, final DescriptorFieldsRepository descriptorFieldsRepository,
            final DescriptorConfigsRepository descriptorConfigsRepository, final FieldValuesRepository fieldValuesRepository) {
        this.registeredDescriptorsRepository = registeredDescriptorsRepository;
        this.descriptorFieldsRepository = descriptorFieldsRepository;
        this.descriptorConfigsRepository = descriptorConfigsRepository;
        this.fieldValuesRepository = fieldValuesRepository;
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

    // TODO update descriptor fields method

    public List<ConfigModel> getConfigsByName(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Optional<RegisteredDescriptorsEntity> registeredDescriptorsEntity = registeredDescriptorsRepository.findFirstByName(descriptorName);
        if (registeredDescriptorsEntity.isPresent()) {
            return getConfigs(Collections.singleton(registeredDescriptorsEntity.get()));
        }
        return getConfigs(Collections.emptyList());
    }

    public List<ConfigModel> getConfigsByType(final String descriptorType) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorType)) {
            throw new AlertDatabaseConstraintException("Descriptor type cannot be empty");
        }
        final List<RegisteredDescriptorsEntity> registeredDescriptorsEntities = registeredDescriptorsRepository.findByType(descriptorType);
        return getConfigs(registeredDescriptorsEntities);
    }

    /**
     * @return the config that was created
     */
    public ConfigModel createEmptyConfig(final String descriptorName) throws AlertDatabaseConstraintException {
        return createConfig(descriptorName, null);
    }

    /**
     * @return the config that was created
     */
    public ConfigModel createConfig(final String descriptorName, final Collection<ConfigFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        final Long descriptorId = getDescriptorIdOrThrowException(descriptorName);
        final DescriptorConfigsEntity descriptorConfigToSave = new DescriptorConfigsEntity(descriptorId);
        final DescriptorConfigsEntity savedDescriptorConfig = descriptorConfigsRepository.save(descriptorConfigToSave);

        final ConfigModel createdConfig = new ConfigModel(descriptorId, savedDescriptorConfig.getId());
        if (configuredFields != null && !configuredFields.isEmpty()) {
            configuredFields.forEach(configuredField -> createdConfig.configuredFields.put(configuredField.getFieldKey(), configuredField));
        }
        return createdConfig;
    }

    /**
     * @return the config resulting from the update
     */
    public ConfigModel updateConfig(final ConfigModel configModel) throws AlertDatabaseConstraintException {
        return updateConfig(configModel.getDescriptorConfigId(), configModel.configuredFields.values());
    }

    /**
     * @return the config after update
     */
    public ConfigModel updateConfig(final Long descriptorConfigId, final Collection<ConfigFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        Objects.requireNonNull(descriptorConfigId, "The config id cannot be null");
        final Optional<DescriptorConfigsEntity> optionalDescriptorConfigsEntity = descriptorConfigsRepository.findById(descriptorConfigId);
        if (optionalDescriptorConfigsEntity.isPresent()) {
            final DescriptorConfigsEntity descriptorConfigsEntity = optionalDescriptorConfigsEntity.get();
            final List<FieldValuesEntity> oldValues = fieldValuesRepository.findByConfigId(descriptorConfigsEntity.getDescriptorId());
            fieldValuesRepository.deleteAll(oldValues);

            final ConfigModel updatedConfig = new ConfigModel(descriptorConfigsEntity.getDescriptorId(), descriptorConfigsEntity.getId());
            if (configuredFields != null && !configuredFields.isEmpty()) {
                for (final ConfigFieldModel configFieldModel : configuredFields) {
                    final Long fieldId = getFieldIdOrThrowException(descriptorConfigsEntity.getDescriptorId(), configFieldModel.getFieldKey());
                    for (final String value : configFieldModel.getFieldValues()) {
                        final FieldValuesEntity newFieldValue = new FieldValuesEntity(descriptorConfigId, fieldId, value);
                        fieldValuesRepository.save(newFieldValue);
                    }
                    updatedConfig.put(configFieldModel);
                }
            }
            return updatedConfig;
        }
        throw new AlertDatabaseConstraintException("A config with that id did not exist");
    }

    public void deleteConfig(final ConfigModel configModel) {
        Objects.requireNonNull(configModel, "Cannot delete a null object from the database");
        deleteConfig(configModel.getDescriptorConfigId());
    }

    public void deleteConfig(final Long descriptorConfigId) {
        Objects.requireNonNull(descriptorConfigId, "The config id cannot be null");
        descriptorConfigsRepository.deleteById(descriptorConfigId);
    }

    // TODO should we replace this with a JOIN?
    private List<ConfigModel> getConfigs(final Collection<RegisteredDescriptorsEntity> descriptors) throws AlertDatabaseConstraintException {
        final List<ConfigModel> configs = new ArrayList<>();
        for (final RegisteredDescriptorsEntity descriptorsEntity : descriptors) {
            final List<DescriptorConfigsEntity> descriptorConfigsEntities = descriptorConfigsRepository.findByDescriptorId(descriptorsEntity.getId());
            for (final DescriptorConfigsEntity descriptorConfigsEntity : descriptorConfigsEntities) {
                final ConfigModel newModel = new ConfigModel(descriptorsEntity.getId(), descriptorConfigsEntity.getId());
                final List<FieldValuesEntity> fieldValuesEntities = fieldValuesRepository.findByConfigId(descriptorConfigsEntity.getId());
                for (final FieldValuesEntity fieldValuesEntity : fieldValuesEntities) {
                    final DescriptorFieldsEntity descriptorFieldsEntity = descriptorFieldsRepository
                                                                                  .findById(fieldValuesEntity.getFieldId())
                                                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("Field id cannot be null"));
                    final String fieldKey = descriptorFieldsEntity.getKey();
                    final ConfigFieldModel fieldModel;
                    if (descriptorFieldsEntity.getSensitive()) {
                        fieldModel = ConfigFieldModel.createSensitive(fieldKey);
                    } else {
                        fieldModel = ConfigFieldModel.create(fieldKey);
                    }
                    fieldModel.setFieldValue(fieldValuesEntity.getValue());
                    newModel.put(fieldModel);
                }
                configs.add(newModel);
            }
        }
        return configs;
    }

    private Long getDescriptorIdOrThrowException(final String descriptorName) throws AlertDatabaseConstraintException {
        if (StringUtils.isEmpty(descriptorName)) {
            throw new AlertDatabaseConstraintException("Descriptor name cannot be empty");
        }
        return registeredDescriptorsRepository
                       .findFirstByName(descriptorName)
                       .map(RegisteredDescriptorsEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("No descriptor with the provided name was registered"));
    }

    private Long getFieldIdOrThrowException(final Long descriptorId, final String fieldKey) throws AlertDatabaseConstraintException {
        if (descriptorId == null) {
            throw new AlertDatabaseConstraintException("Descriptor id cannot be null");
        }
        if (StringUtils.isEmpty(fieldKey)) {
            throw new AlertDatabaseConstraintException("Field key cannot be empty");
        }
        return descriptorFieldsRepository
                       .findFirstByDescriptorIdAndKey(descriptorId, fieldKey)
                       .map(DescriptorFieldsEntity::getId)
                       .orElseThrow(() -> new AlertDatabaseConstraintException("A field with that key did not exist"));
    }

    public class ConfigModel {
        private final Long registeredDescriptorId;
        private final Long descriptorConfigId;
        private final Map<String, ConfigFieldModel> configuredFields;

        private ConfigModel(final Long registeredDescriptorId, final Long descriptorConfigId) {
            this.registeredDescriptorId = registeredDescriptorId;
            this.descriptorConfigId = descriptorConfigId;
            this.configuredFields = new HashMap<>();
        }

        public Long getRegisteredDescriptorId() {
            return registeredDescriptorId;
        }

        public Long getDescriptorConfigId() {
            return descriptorConfigId;
        }

        public void put(final ConfigFieldModel configFieldModel) {
            Objects.requireNonNull(configFieldModel);
            final String fieldKey = configFieldModel.getFieldKey();
            Objects.requireNonNull(fieldKey);
            if (configuredFields.containsKey(fieldKey)) {
                final ConfigFieldModel oldConfigField = configuredFields.get(fieldKey);
                final List<String> values = combine(oldConfigField, configFieldModel);
                oldConfigField.setFieldValues(values);
            }
            configuredFields.put(fieldKey, configFieldModel);
        }

        /**
         * @return true if configFieldModel existed
         */
        public boolean delete(final ConfigFieldModel configFieldModel) {
            Objects.requireNonNull(configFieldModel);
            return delete(configFieldModel.getFieldKey());
        }

        /**
         * @return true if a ConfigFieldModel containing that fieldKey was present
         */
        public boolean delete(final String fieldKey) {
            Objects.requireNonNull(fieldKey);
            final boolean containsField = configuredFields.containsKey(fieldKey);
            configuredFields.remove(fieldKey);
            return containsField;
        }

        private List<String> combine(final ConfigFieldModel first, final ConfigFieldModel second) {
            return Stream.concat(first.getFieldValues().stream(), second.getFieldValues().stream()).collect(Collectors.toList());
        }
    }
}
