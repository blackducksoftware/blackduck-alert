package com.synopsys.integration.alert.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.FieldContextRelation;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldContextRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class FieldContextRepositoryTestIT extends AlertIntegrationTest {
    public static final String CONTEXT_NAME = "TEST_CONTEXT";
    public static final String FIELD_KEY_1 = "field1";
    public static final String FIELD_KEY_2 = "field2";
    @Autowired
    public ConfigContextRepository configContextRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private FieldContextRepository fieldContextRepository;

    @BeforeEach
    public void init() {
        definedFieldRepository.deleteAllInBatch();
        fieldContextRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();

        definedFieldRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        definedFieldRepository.deleteAllInBatch();
        fieldContextRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
    }

    @Test
    public void findByContextId() {
        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.FALSE);
        final DefinedFieldEntity savedFieldEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedFieldEntity2 = definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final ConfigContextEntity configContextEntity1 = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity configContextEntity2 = new ConfigContextEntity(CONTEXT_NAME + "2");
        final ConfigContextEntity savedContextEntity1 = configContextRepository.save(configContextEntity1);
        final ConfigContextEntity savedContextEntity2 = configContextRepository.save(configContextEntity2);
        assertEquals(2, configContextRepository.findAll().size());

        final FieldContextRelation fieldContextRelation1 = new FieldContextRelation(savedFieldEntity1.getId(), savedContextEntity1.getId());
        final FieldContextRelation fieldContextRelation2 = new FieldContextRelation(savedFieldEntity1.getId(), savedContextEntity2.getId());
        final FieldContextRelation fieldContextRelation3 = new FieldContextRelation(savedFieldEntity2.getId(), savedContextEntity1.getId());
        fieldContextRepository.save(fieldContextRelation1);
        fieldContextRepository.save(fieldContextRelation2);
        fieldContextRepository.save(fieldContextRelation3);
        assertEquals(3, fieldContextRepository.findAll().size());

        final List<FieldContextRelation> foundList1 = fieldContextRepository.findByContextId(savedContextEntity1.getId());
        assertEquals(2, foundList1.size());

        final List<FieldContextRelation> foundList2 = fieldContextRepository.findByContextId(savedContextEntity2.getId());
        assertEquals(1, foundList2.size());
    }

    @Test
    public void findByFieldId() {
        final DefinedFieldEntity definedFieldEntity1 = new DefinedFieldEntity(FIELD_KEY_1, Boolean.FALSE);
        final DefinedFieldEntity definedFieldEntity2 = new DefinedFieldEntity(FIELD_KEY_2, Boolean.FALSE);
        final DefinedFieldEntity savedFieldEntity1 = definedFieldRepository.save(definedFieldEntity1);
        final DefinedFieldEntity savedFieldEntity2 = definedFieldRepository.save(definedFieldEntity2);
        assertEquals(2, definedFieldRepository.findAll().size());

        final ConfigContextEntity configContextEntity1 = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity configContextEntity2 = new ConfigContextEntity(CONTEXT_NAME + "2");
        final ConfigContextEntity savedContextEntity1 = configContextRepository.save(configContextEntity1);
        final ConfigContextEntity savedContextEntity2 = configContextRepository.save(configContextEntity2);
        assertEquals(2, configContextRepository.findAll().size());

        final FieldContextRelation fieldContextRelation1 = new FieldContextRelation(savedFieldEntity1.getId(), savedContextEntity1.getId());
        final FieldContextRelation fieldContextRelation2 = new FieldContextRelation(savedFieldEntity1.getId(), savedContextEntity2.getId());
        final FieldContextRelation fieldContextRelation3 = new FieldContextRelation(savedFieldEntity2.getId(), savedContextEntity1.getId());
        fieldContextRepository.save(fieldContextRelation1);
        fieldContextRepository.save(fieldContextRelation2);
        fieldContextRepository.save(fieldContextRelation3);
        assertEquals(3, fieldContextRepository.findAll().size());

        final List<FieldContextRelation> foundList1 = fieldContextRepository.findByFieldId(savedFieldEntity1.getId());
        assertEquals(2, foundList1.size());

        final List<FieldContextRelation> foundList2 = fieldContextRepository.findByFieldId(savedFieldEntity2.getId());
        assertEquals(1, foundList2.size());
    }
}
