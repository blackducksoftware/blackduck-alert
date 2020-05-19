package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
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
        registeredDescriptorRepository.flush();
        configContextRepository.flush();
        descriptorConfigRepository.flush();
        configGroupRepository.flush();

        registeredDescriptorRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        configGroupRepository.deleteAllInBatch();
    }

    @Test
    public void findByJobIdTest() {
        ConfigContextEntity configContextEntity = new ConfigContextEntity(ConfigContextEnum.DISTRIBUTION.name());
        ConfigContextEntity savedConfigContextEntity = configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final String descriptorName = "test descriptor";
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId(), currentTime, currentTime);
        DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId(), currentTime, currentTime);
        DescriptorConfigEntity savedDescriptorConfigEntity1 = descriptorConfigRepository.save(descriptorConfigEntity1);
        DescriptorConfigEntity savedDescriptorConfigEntity2 = descriptorConfigRepository.save(descriptorConfigEntity2);
        assertEquals(2, descriptorConfigRepository.findAll().size());

        UUID jobId = UUID.randomUUID();
        ConfigGroupEntity configGroupEntity1 = new ConfigGroupEntity(savedDescriptorConfigEntity1.getId(), jobId);
        ConfigGroupEntity configGroupEntity2 = new ConfigGroupEntity(savedDescriptorConfigEntity2.getId(), jobId);
        configGroupRepository.save(configGroupEntity1);
        configGroupRepository.save(configGroupEntity2);

        List<ConfigGroupEntity> jobs = configGroupRepository.findByJobId(jobId);
        assertEquals(2, jobs.size());
        assertTrue(jobs.stream().anyMatch(jobEntity -> savedDescriptorConfigEntity1.getId().equals(jobEntity.getConfigId())));
        assertTrue(jobs.stream().anyMatch(jobEntity -> savedDescriptorConfigEntity2.getId().equals(jobEntity.getConfigId())));
    }

    @Test
    public void onDeleteCascadeTest() {
        String context = ConfigContextEnum.DISTRIBUTION.name();
        ConfigContextEntity configContextEntity = new ConfigContextEntity(context);
        ConfigContextEntity savedConfigContextEntity = configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final String descriptorName = "test descriptor";
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId(), currentTime, currentTime);
        descriptorConfigRepository.save(descriptorConfigEntity);
        assertEquals(1, descriptorConfigRepository.findAll().size());

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(descriptorConfigEntity.getId(), UUID.randomUUID());
        configGroupRepository.save(configGroupEntity);
        assertEquals(1, configGroupRepository.findAll().size());

        descriptorConfigRepository.deleteById(descriptorConfigEntity.getId());
        descriptorConfigRepository.flush();
        assertEquals(0, configGroupRepository.findAll().size());
    }

}
