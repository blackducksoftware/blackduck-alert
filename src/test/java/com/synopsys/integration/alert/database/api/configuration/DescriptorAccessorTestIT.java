package com.synopsys.integration.alert.database.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.data.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.data.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.DescriptorMocker;
import com.synopsys.integration.alert.database.api.DescriptorAccessor;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class DescriptorAccessorTestIT extends AlertIntegrationTest {
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

    private DescriptorAccessor descriptorAccessor;

    @BeforeEach
    public void init() {
        descriptorMocker.cleanUpDescriptors();
        descriptorAccessor = new DescriptorAccessor(registeredDescriptorRepository, definedFieldRepository, configContextRepository, descriptorTypeRepository);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    @Test
    public void registerAndGetAllDescriptorsTest() throws AlertDatabaseConstraintException {
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME + "2", DescriptorType.CHANNEL);
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final List<RegisteredDescriptorModel> registeredDescriptors = descriptorAccessor.getRegisteredDescriptors();
        assertEquals(2, registeredDescriptors.size());
    }

    @Test
    public void registerAndGetDescriptorTest() throws AlertDatabaseConstraintException {
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor
                                                                        .getRegisteredDescriptorByName(DESCRIPTOR_NAME)
                                                                        .orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        assertNotNull(registeredDescriptorModel.getId());
        assertEquals(DESCRIPTOR_NAME, registeredDescriptorModel.getName());
    }

    @Test
    public void getRegisteredDescriptorByNameWithEmptyNameTest() {
        getRegisteredDescriptorByNameTestHelper(null);
        getRegisteredDescriptorByNameTestHelper("");
    }

    @Test
    public void getFieldsForDescriptorTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        final DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL, Arrays.asList(field1, field2));
        final List<DefinedFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME, ConfigContextEnum.DISTRIBUTION);
        assertEquals(2, descriptorFields.size());
        assertTrue(descriptorFields.contains(field1));
        assertTrue(descriptorFields.contains(field2));
    }

    @Test
    public void getFieldsForDescriptorByIdTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        final DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.CHANNEL, Arrays.asList(field1, field2));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        final List<DefinedFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptorById(registeredDescriptorModel.getId(), ConfigContextEnum.DISTRIBUTION);
        assertEquals(2, descriptorFields.size());
        assertTrue(descriptorFields.contains(field1));
        assertTrue(descriptorFields.contains(field2));
    }

    @Test
    public void getFieldsForDescriptorWithEmptyArgsTest() {
        getFieldsForDescriptorWithEmptyArgsTestHelper(null);
        getFieldsForDescriptorWithEmptyArgsTestHelper("");
    }

    @Test
    public void getFieldsForDescriptorWithInvalidArgsTest() {
        final String invalidDescriptorName = "-- INVALID DESCRIPTOR NAME --";
        final Long invalidDescriptorId = Long.MAX_VALUE;
        try {
            descriptorAccessor.getFieldsForDescriptor(invalidDescriptorName, ConfigContextEnum.DISTRIBUTION);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("A descriptor with that name did not exist", e.getMessage());
        }
        try {
            descriptorAccessor.getFieldsForDescriptorById(invalidDescriptorId, ConfigContextEnum.DISTRIBUTION);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("A descriptor with that id did not exist", e.getMessage());
        }
    }

    private void getRegisteredDescriptorByNameTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.getRegisteredDescriptorByName(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }

        try {
            descriptorAccessor.getRegisteredDescriptorByName(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void getFieldsForDescriptorWithEmptyArgsTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.DISTRIBUTION);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }
}
