package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class RegisteredDescriptorRepositoryTestIT {
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
        registeredDescriptorRepository.flush();
        registeredDescriptorRepository.deleteAllInBatch();
    }

    @Test
    public void findFirstByNameTest() {
        RegisteredDescriptorEntity entity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, 1L);
        RegisteredDescriptorEntity entity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, 1L);
        registeredDescriptorRepository.save(entity1);
        registeredDescriptorRepository.save(entity2);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        Optional<RegisteredDescriptorEntity> foundEntityOptional = registeredDescriptorRepository.findFirstByName(DESCRIPTOR_NAME_1);
        assertTrue(foundEntityOptional.isPresent());
        RegisteredDescriptorEntity foundEntity = foundEntityOptional.get();
        assertEquals(entity1.getName(), foundEntity.getName());
    }

}
