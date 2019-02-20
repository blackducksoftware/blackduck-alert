package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.data.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.configuration.FieldContextRelation;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

@Transactional
public class DescriptorMocker {
    private final DescriptorTypeRepository descriptorTypeRepository;
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    private final DescriptorFieldRepository descriptorFieldRepository;
    private final DefinedFieldRepository definedFieldRepository;
    private final FieldContextRepository fieldContextRepository;
    private final ConfigContextRepository configContextRepository;

    public DescriptorMocker(final DescriptorTypeRepository descriptorTypeRepository, final RegisteredDescriptorRepository registeredDescriptorRepository,
        final DescriptorFieldRepository descriptorFieldRepository, final DefinedFieldRepository definedFieldRepository, final FieldContextRepository fieldContextRepository,
        final ConfigContextRepository configContextRepository) {
        this.descriptorTypeRepository = descriptorTypeRepository;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.descriptorFieldRepository = descriptorFieldRepository;
        this.definedFieldRepository = definedFieldRepository;
        this.fieldContextRepository = fieldContextRepository;
        this.configContextRepository = configContextRepository;
    }

    public void registerDescriptor(final String descriptorName, final DescriptorType type, final Collection<DefinedFieldModel> fields) {
        registerDescriptor(descriptorName, type);
        for (final DefinedFieldModel field : fields) {
            addFieldToDescriptor(descriptorName, field);
        }
    }

    public void registerDescriptor(final String descriptorName, final DescriptorType type) {
        final Optional<Long> optionalTypeId = descriptorTypeRepository.findFirstByType(type.name()).map(DatabaseEntity::getId);
        final Long typeId;
        if (optionalTypeId.isPresent()) {
            typeId = optionalTypeId.get();
        } else {
            typeId = descriptorTypeRepository.save(new DescriptorTypeEntity(type.name())).getId();
        }
        registeredDescriptorRepository.save(new RegisteredDescriptorEntity(descriptorName, typeId)).getId();
    }

    public void unregisterDescriptor(final String name) {
        registeredDescriptorRepository.findFirstByName(name).ifPresent(registeredDescriptorRepository::delete);
    }

    public void addFieldToDescriptor(final String descriptorName, final DefinedFieldModel definedFieldModel) {
        addFieldToDescriptor(descriptorName, definedFieldModel.getKey(), definedFieldModel.getContexts().stream().findFirst().orElse(ConfigContextEnum.GLOBAL), definedFieldModel.getSensitive());
    }

    public void addFieldToDescriptor(final String descriptorName, final String fieldKey, final ConfigContextEnum context, final Boolean sensitive) {
        addFieldToDescriptor(descriptorName, fieldKey, Set.of(context), sensitive);
    }

    public void addFieldToDescriptor(final String descriptorName, final String fieldKey, final Collection<ConfigContextEnum> contexts, final Boolean sensitive) {
        final Long descriptorId = registeredDescriptorRepository.findFirstByName(descriptorName).map(RegisteredDescriptorEntity::getId).orElseThrow();
        final Long fieldId = definedFieldRepository.findFirstByKey(fieldKey).map(DefinedFieldEntity::getId).orElseGet(() -> definedFieldRepository.save(new DefinedFieldEntity(fieldKey, sensitive)).getId());
        try {
            descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorId, fieldId));
        } catch (final Exception e) {
            // Ignore exception, relation already registered
        }
        for (final ConfigContextEnum context : contexts) {
            final String contextName = context.name();
            final Long contextId = configContextRepository.findFirstByContext(contextName).map(ConfigContextEntity::getId).orElseGet(() -> configContextRepository.save(new ConfigContextEntity(contextName)).getId());
            try {
                fieldContextRepository.save(new FieldContextRelation(fieldId, contextId));
            } catch (final Exception e) {
                // Ignore exception, relation already registered
            }
        }
    }

    public void cleanUpDescriptors() {
        registeredDescriptorRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        // No need to delete relations as they will be deleted by the tables they reference (CASCADE)
    }
}
