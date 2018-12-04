package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorFieldEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class DescriptorFieldRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String DESCRIPTOR_TYPE = "Descriptor Type";
    public static final String DESCRIPTOR_FIELD_KEY_1 = "Test Key 1";
    public static final String DESCRIPTOR_FIELD_KEY_2 = "Test Key 2";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorFieldRepository.deleteAll();
    }

    @Test
    public void findFirstByDescriptorIdAndKeyTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DescriptorFieldEntity descriptorFieldEntity1 = new DescriptorFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DescriptorFieldEntity descriptorFieldEntity2 = new DescriptorFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.TRUE);
        descriptorFieldRepository.save(descriptorFieldEntity1);
        descriptorFieldRepository.save(descriptorFieldEntity2);
        assertEquals(2, descriptorFieldRepository.findAll().size());

        final Optional<DescriptorFieldEntity> descriptorFieldEntityOptional = descriptorFieldRepository.findFirstByDescriptorIdAndKey(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1);
        assertTrue(descriptorFieldEntityOptional.isPresent());
        final DescriptorFieldEntity descriptorFieldEntity = descriptorFieldEntityOptional.get();
        assertEquals(descriptorFieldEntity.getKey(), descriptorFieldEntity1.getKey());
        assertEquals(descriptorFieldEntity.getSensitive(), descriptorFieldEntity1.getSensitive());
    }

    @Test
    public void findByDescriptorIdTest() {
        final RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "Alt", DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        final RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        final DescriptorFieldEntity descriptorFieldEntity1 = new DescriptorFieldEntity(savedDescriptorEntity1.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DescriptorFieldEntity descriptorFieldEntity2 = new DescriptorFieldEntity(savedDescriptorEntity2.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.FALSE);
        final DescriptorFieldEntity descriptorFieldEntity3 = new DescriptorFieldEntity(savedDescriptorEntity1.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.TRUE);
        descriptorFieldRepository.save(descriptorFieldEntity1);
        descriptorFieldRepository.save(descriptorFieldEntity2);
        descriptorFieldRepository.save(descriptorFieldEntity3);
        assertEquals(3, descriptorFieldRepository.findAll().size());

        final List<DescriptorFieldEntity> descriptorFieldEntityList = descriptorFieldRepository.findByDescriptorId(savedDescriptorEntity1.getId());
        assertEquals(2, descriptorFieldEntityList.size());
    }

    @Test
    public void onDeleteCascadeTest() {
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DescriptorFieldEntity descriptorFieldEntity1 = new DescriptorFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_1, Boolean.FALSE);
        final DescriptorFieldEntity descriptorFieldEntity2 = new DescriptorFieldEntity(savedDescriptorEntity.getId(), DESCRIPTOR_FIELD_KEY_2, Boolean.TRUE);
        descriptorFieldRepository.save(descriptorFieldEntity1);
        descriptorFieldRepository.save(descriptorFieldEntity2);
        assertEquals(2, descriptorFieldRepository.findAll().size());

        registeredDescriptorRepository.deleteById(savedDescriptorEntity.getId());
        registeredDescriptorRepository.flush();
        assertEquals(0, descriptorFieldRepository.findAll().size());
    }
}
