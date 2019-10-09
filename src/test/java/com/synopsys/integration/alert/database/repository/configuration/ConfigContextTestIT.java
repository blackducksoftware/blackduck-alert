package com.synopsys.integration.alert.database.repository.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
public class ConfigContextTestIT extends AlertIntegrationTest {
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;

    @BeforeEach
    public void init() {
        registeredDescriptorRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();

        registeredDescriptorRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
    }

    @Test
    public void findFirstByContextTest() {
        final String context = ConfigContextEnum.GLOBAL.name();
        final ConfigContextEntity configContextEntity = new ConfigContextEntity(context);
        configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final Optional<ConfigContextEntity> optionalConfigContextEntity = configContextRepository.findFirstByContext(context);
        assertTrue(optionalConfigContextEntity.isPresent());
        assertEquals(context, optionalConfigContextEntity.get().getContext());
    }

    @Test
    public void onDeleteCascade() {
        final String context = ConfigContextEnum.GLOBAL.name();
        final ConfigContextEntity configContextEntity = new ConfigContextEntity(context);
        final ConfigContextEntity savedConfigContextEntity = configContextRepository.save(configContextEntity);
        assertEquals(1, configContextRepository.findAll().size());

        final String descriptorName = "test descriptor";
        final RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName, 1L);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        Date currentTime = DateUtils.createCurrentDateTimestamp();
        final DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId(), currentTime, currentTime);
        descriptorConfigRepository.save(descriptorConfigEntity);
        assertEquals(1, descriptorConfigRepository.findAll().size());

        configContextRepository.deleteById(savedConfigContextEntity.getId());
        configContextRepository.flush();
        assertEquals(0, descriptorConfigRepository.findAll().size());
    }
}
