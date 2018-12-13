package com.synopsys.integration.alert.repository.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;

public class ConfigContextTestIT extends AlertIntegrationTest {
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorConfigRepository.deleteAll();
        configContextRepository.deleteAll();
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
        final RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity(descriptorName);
        final RegisteredDescriptorEntity savedRegisteredDescriptorEntity = registeredDescriptorRepository.save(registeredDescriptorEntity);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        final DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(savedRegisteredDescriptorEntity.getId(), savedConfigContextEntity.getId());
        descriptorConfigRepository.save(descriptorConfigEntity);
        assertEquals(1, descriptorConfigRepository.findAll().size());

        configContextRepository.deleteById(savedConfigContextEntity.getId());
        configContextRepository.flush();
        assertEquals(0, descriptorConfigRepository.findAll().size());
    }
}
