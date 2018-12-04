package com.synopsys.integration.alert.repository.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.descriptor.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class DescriptorConfigRepositoryTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String DESCRIPTOR_TYPE = "Descriptor Type";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorConfigRepository.deleteAll();
    }

    @Test
    public void findByDescriptorIdTest() {
        final RegisteredDescriptorEntity descriptorEntity1 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity descriptorEntity2 = new RegisteredDescriptorEntity(DESCRIPTOR_NAME + "Alt", DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity1 = registeredDescriptorRepository.save(descriptorEntity1);
        final RegisteredDescriptorEntity savedDescriptorEntity2 = registeredDescriptorRepository.save(descriptorEntity2);

        final DescriptorConfigEntity descriptorConfigEntity1 = new DescriptorConfigEntity(savedDescriptorEntity1.getId());
        final DescriptorConfigEntity descriptorConfigEntity2 = new DescriptorConfigEntity(savedDescriptorEntity1.getId());
        final DescriptorConfigEntity descriptorConfigEntity3 = new DescriptorConfigEntity(savedDescriptorEntity2.getId());
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
        final RegisteredDescriptorEntity descriptorEntity = new RegisteredDescriptorEntity(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorEntity savedDescriptorEntity = registeredDescriptorRepository.save(descriptorEntity);

        final DescriptorConfigEntity descriptorFieldEntity1 = new DescriptorConfigEntity(savedDescriptorEntity.getId());
        final DescriptorConfigEntity descriptorFieldEntity2 = new DescriptorConfigEntity(savedDescriptorEntity.getId());
        descriptorConfigRepository.save(descriptorFieldEntity1);
        descriptorConfigRepository.save(descriptorFieldEntity2);
        assertEquals(2, descriptorConfigRepository.findAll().size());

        registeredDescriptorRepository.deleteById(savedDescriptorEntity.getId());
        registeredDescriptorRepository.flush();
        assertEquals(0, descriptorConfigRepository.findAll().size());
    }

}
