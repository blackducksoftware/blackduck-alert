package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class DefinedFieldRepositoryTestIT {
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
        registeredDescriptorRepository.flush();
        descriptorFieldRepository.flush();
        definedFieldRepository.flush();

        registeredDescriptorRepository.deleteAllInBatch();
        descriptorFieldRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();
    }

    @Test
    public void findFirstByKeyTest() {
        DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.TRUE);
        definedFieldRepository.save(definedFieldEntity1);
        definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        Optional<DefinedFieldEntity> descriptorFieldEntityOptional = definedFieldRepository.findFirstByKey(FIELD_KEY_1);
        assertTrue(descriptorFieldEntityOptional.isPresent());
        DefinedFieldEntity definedFieldEntity = descriptorFieldEntityOptional.get();
        assertEquals(definedFieldEntity.getKey(), definedFieldEntity1.getKey());
        assertEquals(definedFieldEntity.getSensitive(), definedFieldEntity1.getSensitive());
    }

    @Test
    public void onDeleteCascadeTest() {
        final String descriptorName = "test descriptor";
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        DefinedFieldEntity savedDefinedFieldEntity = definedFieldRepository.save(definedFieldEntity);
        assertEquals(1, definedFieldRepository.findAll().size());

        DescriptorFieldRelation descriptorFieldRelation = new DescriptorFieldRelation(savedRegisteredDescriptorEntity.getId(), savedDefinedFieldEntity.getId());
        descriptorFieldRepository.save(descriptorFieldRelation);
        assertEquals(1, descriptorFieldRepository.findAll().size());

        definedFieldRepository.deleteById(savedDefinedFieldEntity.getId());
        definedFieldRepository.flush();
        assertEquals(0, descriptorFieldRepository.findAll().size());
    }

}
