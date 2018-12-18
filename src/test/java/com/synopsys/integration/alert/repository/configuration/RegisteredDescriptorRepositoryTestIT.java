package com.synopsys.integration.alert.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class RegisteredDescriptorRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME_1 = "name1";
    public static final String DESCRIPTOR_NAME_2 = "name2";

    @Autowired
    public RegisteredDescriptorRepository registeredDescriptorRepository;

    @BeforeEach
    public void init() {
        registeredDescriptorRepository.deleteAllInBatch();
        registeredDescriptorRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
    }

    @Test
    public void findFirstByNameTest() {
        final RegisteredDescriptorEntity entity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, 1L);
        final RegisteredDescriptorEntity entity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, 1L);
        registeredDescriptorRepository.save(entity1);
        registeredDescriptorRepository.save(entity2);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final Optional<RegisteredDescriptorEntity> foundEntityOptional = registeredDescriptorRepository.findFirstByName(DESCRIPTOR_NAME_1);
        assertTrue(foundEntityOptional.isPresent());
        final RegisteredDescriptorEntity foundEntity = foundEntityOptional.get();
        assertEquals(entity1.getName(), foundEntity.getName());
    }

    // TODO onDeleteCascadeTest
}
