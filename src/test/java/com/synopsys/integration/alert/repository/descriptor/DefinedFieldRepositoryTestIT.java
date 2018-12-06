package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class DefinedFieldRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String DESCRIPTOR_TYPE = "Descriptor Type";
    public static final String DESCRIPTOR_FIELD_KEY_1 = "Test Key 1";
    public static final String DESCRIPTOR_FIELD_KEY_2 = "Test Key 2";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        definedFieldRepository.deleteAll();
    }

    @Test
    public void findFirstByDescriptorIdAndKeyTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.TRUE);
        definedFieldRepository.save(definedFieldEntity1);
        definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final Optional<DefinedFieldEntity> descriptorFieldEntityOptional = definedFieldRepository.findFirstByDescriptorIdAndKey(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1);
        assertTrue(descriptorFieldEntityOptional.isPresent());
        final DefinedFieldEntity definedFieldEntity = descriptorFieldEntityOptional.get();
        assertEquals(definedFieldEntity.getKey(), definedFieldEntity1.getKey());
        assertEquals(definedFieldEntity.getSensitive(), definedFieldEntity1.getSensitive());
    }

    @Test
    public void findByDescriptorIdTest() {
        final RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "Alt", DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        final RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(savedDescriptorEntity1.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(savedDescriptorEntity2.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity3 = new DefinedFieldEntity(savedDescriptorEntity1.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.TRUE);
        definedFieldRepository.save(definedFieldEntity1);
        definedFieldRepository.save(definedFieldEntity2);
        definedFieldRepository.save(definedFieldEntity3);
        assertEquals(3, definedFieldRepository.findAll().size());

        final List<DefinedFieldEntity> definedFieldEntityList = definedFieldRepository.findByDescriptorId(savedDescriptorEntity1.getId());
        assertEquals(2, definedFieldEntityList.size());
    }

    @Test
    public void onDeleteCascadeTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.TRUE);
        definedFieldRepository.save(definedFieldEntity1);
        definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        registeredDescriptorRepository.deleteById(savedDescriptorEntity.getId());
        registeredDescriptorRepository.flush();
        assertEquals(0, definedFieldRepository.findAll().size());
    }
}
