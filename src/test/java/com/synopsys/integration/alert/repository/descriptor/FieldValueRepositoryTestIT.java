package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.entity.descriptor.FieldValueEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class FieldValueRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String CONTEXT_NAME = "TEST_CONTEXT";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorFieldRepository.deleteAll();
        definedFieldRepository.deleteAll();
        descriptorConfigRepository.deleteAll();
        configContextRepository.deleteAll();
        fieldValueRepository.deleteAll();
    }

    @Test
    public void findByConfigIdTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity("fieldKey1", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity("fieldKey2", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity3 = new DefinedFieldEntity("fieldKey3", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity4 = new DefinedFieldEntity("fieldKey4", Boolean.FALSE);
        final DefinedFieldEntity savedEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedEntity2 = definedFieldRepository.save(definedFieldEntity2);
        final DefinedFieldEntity savedEntity3 = definedFieldRepository.save(definedFieldEntity3);
        final DefinedFieldEntity savedEntity4 = definedFieldRepository.save(definedFieldEntity4);
        assertEquals(4, definedFieldRepository.findAll().size());

        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity1.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity2.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity3.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity4.getId()));

        final ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        final DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedDescriptorEntity.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedDescriptorEntity.getId(), savedContextEntity.getId());
        descriptorConfigRepository.save(descriptorConfigEntity1);
        descriptorConfigRepository.save(descriptorConfigEntity2);
        assertEquals(2, descriptorConfigRepository.findAll().size());

        final FieldValueEntity fieldValueEntity1 = new FieldValueEntity(descriptorConfigEntity1.getId(), savedEntity1.getId(), "value1");
        final FieldValueEntity fieldValueEntity2 = new FieldValueEntity(descriptorConfigEntity1.getId(), savedEntity2.getId(), "value2");
        final FieldValueEntity fieldValueEntity3 = new FieldValueEntity(descriptorConfigEntity2.getId(), savedEntity3.getId(), "value3");
        final FieldValueEntity fieldValueEntity4 = new FieldValueEntity(descriptorConfigEntity2.getId(), savedEntity4.getId(), "value4");
        fieldValueRepository.save(fieldValueEntity1);
        fieldValueRepository.save(fieldValueEntity2);
        fieldValueRepository.save(fieldValueEntity3);
        fieldValueRepository.save(fieldValueEntity4);
        assertEquals(4, fieldValueRepository.findAll().size());

        final List<FieldValueEntity> fieldValueEntityList1 = fieldValueRepository.findByConfigId(descriptorConfigEntity1.getId());
        assertEquals(2, fieldValueEntityList1.size());

        final List<FieldValueEntity> fieldValueEntityList2 = fieldValueRepository.findByConfigId(descriptorConfigEntity2.getId());
        assertEquals(2, fieldValueEntityList2.size());
    }

    @Test
    public void onDeleteCascadeTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity("fieldKey1", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity("fieldKey2", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity3 = new DefinedFieldEntity("fieldKey3", Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity4 = new DefinedFieldEntity("fieldKey4", Boolean.FALSE);
        final DefinedFieldEntity savedEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedEntity2 = definedFieldRepository.save(definedFieldEntity2);
        final DefinedFieldEntity savedEntity3 = definedFieldRepository.save(definedFieldEntity3);
        final DefinedFieldEntity savedEntity4 = definedFieldRepository.save(definedFieldEntity4);
        assertEquals(4, definedFieldRepository.findAll().size());

        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity1.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity2.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity3.getId()));
        descriptorFieldRepository.save(new DescriptorFieldRelation(descriptorEntity.getId(), savedEntity4.getId()));

        final ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        final DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedDescriptorEntity.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedDescriptorEntity.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity3 = new DescriptorConfigEntity(savedDescriptorEntity.getId(), savedContextEntity.getId());
        descriptorConfigRepository.save(descriptorConfigEntity1);
        descriptorConfigRepository.save(descriptorConfigEntity2);
        descriptorConfigRepository.save(descriptorConfigEntity3);
        assertEquals(3, descriptorConfigRepository.findAll().size());

        final FieldValueEntity fieldValueEntity1 = new FieldValueEntity(descriptorConfigEntity1.getId(), savedEntity1.getId(), "value1");
        final FieldValueEntity fieldValueEntity2 = new FieldValueEntity(descriptorConfigEntity1.getId(), savedEntity2.getId(), "value2");
        final FieldValueEntity fieldValueEntity3 = new FieldValueEntity(descriptorConfigEntity2.getId(), savedEntity3.getId(), "value3");
        final FieldValueEntity fieldValueEntity4 = new FieldValueEntity(descriptorConfigEntity3.getId(), savedEntity4.getId(), "value4");
        fieldValueRepository.save(fieldValueEntity1);
        fieldValueRepository.save(fieldValueEntity2);
        fieldValueRepository.save(fieldValueEntity3);
        fieldValueRepository.save(fieldValueEntity4);
        assertEquals(4, fieldValueRepository.findAll().size());

        // Delete a configuration (deletes 1 & 2)
        descriptorConfigRepository.deleteById(descriptorConfigEntity1.getId());
        descriptorConfigRepository.flush();
        assertEquals(2, fieldValueRepository.findAll().size());

        // Delete a descriptor field (deletes 3)
        definedFieldRepository.deleteById(savedEntity3.getId());
        definedFieldRepository.flush();
        assertEquals(1, fieldValueRepository.findAll().size());

        // Delete a descriptor (deletes 4)
        registeredDescriptorRepository.deleteById(savedDescriptorEntity.getId());
        registeredDescriptorRepository.flush();
        assertEquals(0, fieldValueRepository.findAll().size());
    }
}
