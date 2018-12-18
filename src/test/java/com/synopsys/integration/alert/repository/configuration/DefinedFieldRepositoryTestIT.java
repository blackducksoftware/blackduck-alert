package com.synopsys.integration.alert.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class DefinedFieldRepositoryTestIT extends AlertIntegrationTest {
    public static final String FIELD_KEY_1 = "Test Key 1";
    public static final String FIELD_KEY_2 = "Test Key 2";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;

    @BeforeEach
    public void init() {
        registeredDescriptorRepository.deleteAllInBatch();
        descriptorFieldRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();

        registeredDescriptorRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
        descriptorFieldRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();
    }

    @Test
    public void findFirstByKeyTest() {
        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.TRUE);
        definedFieldRepository.save(definedFieldEntity1);
        definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final Optional<DefinedFieldEntity> descriptorFieldEntityOptional = definedFieldRepository.findFirstByKey(FIELD_KEY_1);
        assertTrue(descriptorFieldEntityOptional.isPresent());
        final DefinedFieldEntity definedFieldEntity = descriptorFieldEntityOptional.get();
        assertEquals(definedFieldEntity.getKey(), definedFieldEntity1.getKey());
        assertEquals(definedFieldEntity.getSensitive(), definedFieldEntity1.getSensitive());
    }

    @Test
    public void onDeleteCascadeTest() {
        final String descriptorName = "test descriptor";
        final RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        final DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity savedDefinedFieldEntity = definedFieldRepository.save(definedFieldEntity);
        assertEquals(1, definedFieldRepository.findAll().size());

        final DescriptorFieldRelation descriptorFieldRelation = new DescriptorFieldRelation(savedRegisteredDescriptorEntity.getId(), savedDefinedFieldEntity.getId());
        descriptorFieldRepository.save(descriptorFieldRelation);
        assertEquals(1, descriptorFieldRepository.findAll().size());

        definedFieldRepository.deleteById(savedDefinedFieldEntity.getId());
        definedFieldRepository.flush();
        assertEquals(0, descriptorFieldRepository.findAll().size());
    }
}
