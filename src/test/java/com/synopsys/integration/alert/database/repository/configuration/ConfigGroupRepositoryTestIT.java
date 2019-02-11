package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class ConfigGroupRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private ConfigGroupRepository configGroupRepository;

    @BeforeEach
    public void init() {
        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        configGroupRepository.deleteAllInBatch();

        registeredDescriptorRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        configGroupRepository.deleteAllInBatch();
    }

    @Test
    public void findByJobIdTest() {
        final ConfigContextEntity configContextEntity = new ConfigContextEntity(ConfigContextEnum.DISTRIBUTION.name());
        final ConfigContextEntity savedConfigContextEntity = configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final String descriptorName = "test descriptor";
        final RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        final DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId());
        final DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId());
        final DescriptorConfigEntity savedDescriptorConfigEntity1 = descriptorConfigRepository.save(descriptorConfigEntity1);
        final DescriptorConfigEntity savedDescriptorConfigEntity2 = descriptorConfigRepository.save(descriptorConfigEntity2);
        assertEquals(2, descriptorConfigRepository.findAll().size());

        final UUID jobId = UUID.randomUUID();
        final ConfigGroupEntity configGroupEntity1 = new ConfigGroupEntity(savedDescriptorConfigEntity1.getId(), jobId);
        final ConfigGroupEntity configGroupEntity2 = new ConfigGroupEntity(savedDescriptorConfigEntity2.getId(), jobId);
        configGroupRepository.save(configGroupEntity1);
        configGroupRepository.save(configGroupEntity2);

        final List<ConfigGroupEntity> jobs = configGroupRepository.findByJobId(jobId);
        assertEquals(2, jobs.size());
        assertTrue(jobs.stream().anyMatch(jobEntity -> savedDescriptorConfigEntity1.getId().equals(jobEntity.getConfigId())));
        assertTrue(jobs.stream().anyMatch(jobEntity -> savedDescriptorConfigEntity2.getId().equals(jobEntity.getConfigId())));
    }

    @Test
    public void onDeleteCascadeTest() {
        final String context = ConfigContextEnum.DISTRIBUTION.name();
        final ConfigContextEntity configContextEntity = new ConfigContextEntity(context);
        final ConfigContextEntity savedConfigContextEntity = configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final String descriptorName = "test descriptor";
        final RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        final DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId());
        descriptorConfigRepository.save(descriptorConfigEntity);
        assertEquals(1, descriptorConfigRepository.findAll().size());

        final ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(descriptorConfigEntity.getId(), UUID.randomUUID());
        configGroupRepository.save(configGroupEntity);
        assertEquals(1, configGroupRepository.findAll().size());

        descriptorConfigRepository.deleteById(descriptorConfigEntity.getId());
        descriptorConfigRepository.flush();
        assertEquals(0, configGroupRepository.findAll().size());
    }

}
