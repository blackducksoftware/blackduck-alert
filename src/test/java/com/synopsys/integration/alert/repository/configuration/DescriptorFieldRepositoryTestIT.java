package com.synopsys.integration.alert.repository.configuration;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorFieldRelation;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;

public class DescriptorFieldRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME_1 = "name1";
    public static final String DESCRIPTOR_NAME_2 = "name2";
    public static final String FIELD_KEY_1 = "field1";
    public static final String FIELD_KEY_2 = "field2";

    @Autowired
    public RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    public DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    public DefinedFieldRepository definedFieldRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorFieldRepository.deleteAll();
        definedFieldRepository.deleteAll();
    }

    @Test
    public void findByDescriptorIdTest() {
        final RegisteredDescriptorEntity registeredDescriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, 1L);
        final RegisteredDescriptorEntity registeredDescriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity1 = registeredDescriptorRepository.save(registeredDescriptorEntity1);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity2 = registeredDescriptorRepository.save(registeredDescriptorEntity2);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.FALSE);
        final DefinedFieldEntity savedFieldEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedFieldEntity2 = definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final DescriptorFieldRelation descriptorFieldRelation1 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity1.getId(), savedFieldEntity1.getId());
        final DescriptorFieldRelation descriptorFieldRelation2 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity1.getId(), savedFieldEntity2.getId());
        final DescriptorFieldRelation descriptorFieldRelation3 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity2.getId(), savedFieldEntity1.getId());
        descriptorFieldRepository.save(descriptorFieldRelation1);
        descriptorFieldRepository.save(descriptorFieldRelation2);
        descriptorFieldRepository.save(descriptorFieldRelation3);

        // Should find a relation to field 1 and 2
        final List<DescriptorFieldRelation> foundList1 = descriptorFieldRepository.findByDescriptorId(savedRegisteredDescriptorEntity1.getId());
        assertEquals(2, foundList1.size());

        // Should find a relation to field 1
        final List<DescriptorFieldRelation> foundList2 = descriptorFieldRepository.findByDescriptorId(savedRegisteredDescriptorEntity2.getId());
        assertEquals(1, foundList2.size());
    }

    @Test
    public void findByFieldIdTest() {
        final RegisteredDescriptorEntity registeredDescriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_1, 1L);
        final RegisteredDescriptorEntity registeredDescriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME_2, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity1 = registeredDescriptorRepository.save(registeredDescriptorEntity1);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity2 = registeredDescriptorRepository.save(registeredDescriptorEntity2);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.FALSE);
        final DefinedFieldEntity savedFieldEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedFieldEntity2 = definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final DescriptorFieldRelation descriptorFieldRelation1 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity1.getId(), savedFieldEntity1.getId());
        final DescriptorFieldRelation descriptorFieldRelation2 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity1.getId(), savedFieldEntity2.getId());
        final DescriptorFieldRelation descriptorFieldRelation3 = new DescriptorFieldRelation(savedRegisteredDescriptorEntity2.getId(), savedFieldEntity1.getId());
        descriptorFieldRepository.save(descriptorFieldRelation1);
        descriptorFieldRepository.save(descriptorFieldRelation2);
        descriptorFieldRepository.save(descriptorFieldRelation3);

        // Should find a relation to descriptor 1 and 2
        final List<DescriptorFieldRelation> foundList1 = descriptorFieldRepository.findByFieldId(savedFieldEntity1.getId());
        assertEquals(2, foundList1.size());

        // Should find a relation to descriptor 1
        final List<DescriptorFieldRelation> foundList2 = descriptorFieldRepository.findByFieldId(savedFieldEntity2.getId());
        assertEquals(1, foundList2.size());
    }
}
