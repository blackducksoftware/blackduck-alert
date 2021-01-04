package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class DescriptorConfigRepositoryTestIT {
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
        registeredDescriptorRepository.flush();
        configContextRepository.flush();
        descriptorConfigRepository.flush();
        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
    }

    @Test
    public void findByDescriptorIdTest() {
        RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, 1L);
        RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "Alt", 1L);
        RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId(), currentTime, currentTime);
        DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId(), currentTime, currentTime);
        DescriptorConfigEntity descriptorConfigEntity3 = new DescriptorConfigEntity(savedDescriptorEntity2.getId(), savedContextEntity.getId(), currentTime, currentTime);
        descriptorConfigRepository.save(descriptorConfigEntity1);
        descriptorConfigRepository.save(descriptorConfigEntity2);
        descriptorConfigRepository.save(descriptorConfigEntity3);

        List<DescriptorConfigEntity> descriptorConfig1List = descriptorConfigRepository.findByDescriptorId(savedDescriptorEntity1.getId());
        assertEquals(2, descriptorConfig1List.size());

        List<DescriptorConfigEntity> descriptorConfig2List = descriptorConfigRepository.findByDescriptorId(savedDescriptorEntity2.getId());
        assertEquals(1, descriptorConfig2List.size());
    }

    @Test
    public void onDeleteCascadeTest() {
        RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, 1L);
        RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "2", 1L);
        RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        ConfigContextEntity configContextEntity = new ConfigContextEntity(CONTEXT_NAME);
        ConfigContextEntity savedContextEntity = configContextRepository.save(configContextEntity);

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorFieldEntity1 = new DescriptorConfigEntity(savedDescriptorEntity1.getId(), savedContextEntity.getId(), currentTime, currentTime);
        DescriptorConfigEntity descriptorFieldEntity2 = new DescriptorConfigEntity(savedDescriptorEntity2.getId(), savedContextEntity.getId(), currentTime, currentTime);
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
