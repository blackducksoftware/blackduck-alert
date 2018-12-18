package com.synopsys.integration.alert.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class DescriptorConfigRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String CONTEXT_NAME = "TEST_CONTEXT";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;

    @BeforeEach
    public void init() {
        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();

        registeredDescriptorRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
    }

    @Test
    public void findByDescriptorIdTest() {
        final RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, 1L);
        final RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "Alt", 1L);
        final RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        final RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        final ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        final DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity3 = new DescriptorConfigEntity(savedDescriptorEntity2.getId(), savedContextEntity.getId());
        descriptorConfigRepository.save(descriptorConfigEntity1);
        descriptorConfigRepository.save(descriptorConfigEntity2);
        descriptorConfigRepository.save(descriptorConfigEntity3);

        final List<DescriptorConfigEntity> descriptorConfig1List = descriptorConfigRepository.findByDescriptorId(savedDescriptorEntity1.getId());
        assertEquals(2, descriptorConfig1List.size());

        final List<DescriptorConfigEntity> descriptorConfig2List = descriptorConfigRepository.findByDescriptorId(savedDescriptorEntity2.getId());
        assertEquals(1, descriptorConfig2List.size());
    }

    @Test
    public void onDeleteCascadeTest() {
        final RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, 1L);
        final RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "2", 1L);
        final RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        final RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        final ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        final ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        final DescriptorConfigEntity descriptorFieldEntity1 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId());
        final DescriptorConfigEntity descriptorFieldEntity2 = new DescriptorConfigEntity(savedDescriptorEntity2.getId(), savedContextEntity.getId());
        descriptorConfigRepository.save(descriptorFieldEntity1);
        descriptorConfigRepository.save(descriptorFieldEntity2);
        assertEquals(2, descriptorConfigRepository.findAll().size());

        registeredDescriptorRepository.deleteById(savedDescriptorEntity1.getId());
        registeredDescriptorRepository.flush();
        assertEquals(1, descriptorConfigRepository.findAll().size());

        configContextRepository.deleteById(savedContextEntity.getId());
        configContextRepository.flush();
        assertEquals(0, descriptorConfigRepository.findAll().size());
    }

}
