package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.DefinedFieldEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class DefinedFieldRepositoryTestIT extends AlertIntegrationTest {
    public static final String FIELD_KEY_1 = "Test Key 1";
    public static final String FIELD_KEY_2 = "Test Key 2";

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
}
