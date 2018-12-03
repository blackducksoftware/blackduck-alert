package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class RegisteredDescriptorRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME_1 = "name1";
    public static final String DESCRIPTOR_NAME_2 = "name2";
    public static final String DESCRIPTOR_NAME_3 = "name2";
    public static final String DESCRIPTOR_TYPE_A = "typeA";
    public static final String DESCRIPTOR_TYPE_B = "typeB";

    @Autowired
    public RegisteredDescriptorRepository registeredDescriptorRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
    }

    @Test
    public void findFirstByNameTest() {
        final RegisteredDescriptorEntity entity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, DESCRIPTOR_TYPE_A);
        final RegisteredDescriptorEntity entity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, DESCRIPTOR_TYPE_A);
        registeredDescriptorRepository.save(entity1);
        registeredDescriptorRepository.save(entity2);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final Optional<RegisteredDescriptorEntity> foundEntityOptional = registeredDescriptorRepository.findFirstByName(DESCRIPTOR_NAME_1);
        assertTrue(foundEntityOptional.isPresent());
        final RegisteredDescriptorEntity foundEntity = foundEntityOptional.get();
        assertEquals(entity1.getName(), foundEntity.getName());
        assertEquals(entity1.getType(), foundEntity.getType());
    }

    @Test
    public void findByTypeTest() {
        final RegisteredDescriptorEntity entity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, DESCRIPTOR_TYPE_A);
        final RegisteredDescriptorEntity entity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, DESCRIPTOR_TYPE_A);
        final RegisteredDescriptorEntity entity3 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_3, DESCRIPTOR_TYPE_B);
        registeredDescriptorRepository.save(entity1);
        registeredDescriptorRepository.save(entity2);
        registeredDescriptorRepository.save(entity3);
        assertEquals(3, registeredDescriptorRepository.findAll().size());

        final List<RegisteredDescriptorEntity> foundEntitiesA = registeredDescriptorRepository.findByType(DESCRIPTOR_TYPE_A);
        assertEquals(2, foundEntitiesA.size());
        
        final List<RegisteredDescriptorEntity> foundEntitiesB = registeredDescriptorRepository.findByType(DESCRIPTOR_TYPE_B);
        assertEquals(1, foundEntitiesB.size());
    }
}
