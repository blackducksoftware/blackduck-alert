package com.synopsys.integration.alert.database.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.api.DefaultDescriptorAccessor;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Transactional
@AlertIntegrationTest
public class DescriptorAccessorTestIT {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorTypeRepository descriptorTypeRepository;
    @Autowired
    private DescriptorMocker descriptorMocker;

    private DefaultDescriptorAccessor descriptorAccessor;

    @BeforeEach
    public void init() {
        descriptorMocker.cleanUpDescriptors();
        descriptorAccessor = new DefaultDescriptorAccessor(registeredDescriptorRepository, definedFieldRepository, configContextRepository, descriptorTypeRepository);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    private DescriptorKey createDescriptorKey() {
        return new DescriptorKey(DESCRIPTOR_NAME, DESCRIPTOR_NAME) {};
    }

    @Test
    public void registerAndGetAllDescriptorsTest() {
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME + "2", DescriptorType.CHANNEL);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        List<RegisteredDescriptorModel> registeredDescriptors = descriptorAccessor.getRegisteredDescriptors();
        assertEquals(2, registeredDescriptors.size());
    }

    @Test
    public void registerAndGetDescriptorTest() {
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor
                                                                  .getRegisteredDescriptorByKey(createDescriptorKey())
                                                                  .orElseThrow(() -> new AlertRuntimeException("This descriptor should exist"));
        assertNotNull(registeredDescriptorModel.getId());
        assertEquals(DESCRIPTOR_NAME, registeredDescriptorModel.getName());
    }

    @Test
    public void getFieldsForDescriptorTest() {
        final String field1Key = "field1";
        final String field2Key = "field2";
        DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL, Arrays.asList(field1, field2));
        List<DefinedFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptor(createDescriptorKey(), ConfigContextEnum.DISTRIBUTION);
        assertEquals(2, descriptorFields.size());
        assertTrue(descriptorFields.contains(field1));
        assertTrue(descriptorFields.contains(field2));
    }

    @Test
    public void getFieldsForDescriptorByIdTest() {
        final String field1Key = "field1";
        final String field2Key = "field2";
        DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL, Arrays.asList(field1, field2));
        RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByKey(createDescriptorKey())
                                                                  .orElseThrow(() -> new AlertRuntimeException("This descriptor should exist"));
        List<DefinedFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptorById(registeredDescriptorModel.getId(), ConfigContextEnum.DISTRIBUTION);
        assertEquals(2, descriptorFields.size());
        assertTrue(descriptorFields.contains(field1));
        assertTrue(descriptorFields.contains(field2));
    }

}
