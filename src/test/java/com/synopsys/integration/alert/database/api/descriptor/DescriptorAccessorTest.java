package com.synopsys.integration.alert.database.api.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.descriptor.DescriptorAccessor.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class DescriptorAccessorTest extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String DESCRIPTOR_TYPE = "Test Component";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;

    private DescriptorAccessor descriptorAccessor;

    @Before
    public void init() {
        descriptorAccessor = new DescriptorAccessor(registeredDescriptorRepository, descriptorFieldRepository);
    }

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAll();
        descriptorFieldRepository.deleteAll();
    }

    @Test
    public void registerAndGetAllDescriptorsTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME + "2", DESCRIPTOR_TYPE + "2");
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final List<RegisteredDescriptorModel> registeredDescriptors = descriptorAccessor.getRegisteredDescriptors();
        assertEquals(2, registeredDescriptors.size());
    }

    @Test
    public void registerAndGetDescriptorTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor
                                                                            .getRegisteredDescriptorByName(DESCRIPTOR_NAME)
                                                                            .orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        assertNotNull(registeredDescriptorModel.getId());
        assertEquals(DESCRIPTOR_NAME, registeredDescriptorModel.getName());
        assertEquals(DESCRIPTOR_TYPE, registeredDescriptorModel.getType());
    }

    @Test
    public void registerDescriptorWithFieldsTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        final DescriptorFieldModel field2 = new DescriptorFieldModel(field2Key, Boolean.TRUE);

        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1, field2));
        assertEquals(1, registeredDescriptorRepository.findAll().size());
        assertEquals(2, descriptorFieldRepository.findAll().size());
    }

    @Test
    public void registerDescriptorThatAlreadyExistsTest() throws AlertDatabaseConstraintException {
        final boolean initialResult = descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        assertTrue(initialResult);
        final boolean reAddResult = descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        assertFalse(reAddResult);
    }

    @Test
    public void getRegisteredDescriptorByNameWithEmptyNameTest() {
        getRegisteredDescriptorByNameTestHelper(null);
        getRegisteredDescriptorByNameTestHelper("");
    }

    @Test
    public void unregisterDescriptorTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
        assertEquals(1, registeredDescriptorRepository.findAll().size());

        descriptorAccessor.unregisterDescriptor(DESCRIPTOR_NAME);
        assertEquals(0, registeredDescriptorRepository.findAll().size());
    }

    @Test
    public void unregisterDescriptorThatDoesNotExistTest() throws AlertDatabaseConstraintException {
        final boolean result = descriptorAccessor.unregisterDescriptor(DESCRIPTOR_NAME);
        assertFalse(result);
    }

    @Test
    public void unregisterDescriptorWithEmptyNameTest() {
        unregisterDescriptorTestHelper(null);
        unregisterDescriptorTestHelper("");
    }

    @Test
    public void getFieldsForDescriptorTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        final DescriptorFieldModel field2 = new DescriptorFieldModel(field2Key, Boolean.TRUE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1, field2));
        final List<DescriptorFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME);
        assertEquals(2, descriptorFields.size());
        assertTrue(descriptorFields.contains(field1));
        assertTrue(descriptorFields.contains(field2));
    }

    @Test
    public void getFieldsForDescriptorByIdTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        final DescriptorFieldModel field2 = new DescriptorFieldModel(field2Key, Boolean.TRUE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1, field2));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        final List<DescriptorFieldModel> descriptorFields = descriptorAccessor.getFieldsForDescriptorById(registeredDescriptorModel.getId());
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
            descriptorAccessor.getFieldsForDescriptor(invalidDescriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("A descriptor with that name did not exist", e.getMessage());
        }
        try {
            descriptorAccessor.getFieldsForDescriptorById(invalidDescriptorId);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("A descriptor with that id did not exist", e.getMessage());
        }
    }

    @Test
    public void addDescriptorFieldTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

        final List<DescriptorFieldModel> initialFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME);
        assertEquals(1, initialFields.size());

        final String field2Key = "field2";
        final DescriptorFieldModel field2 = new DescriptorFieldModel(field2Key, Boolean.TRUE);
        descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), field2);

        final List<DescriptorFieldModel> fieldsAfterAdd = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME);
        assertEquals(2, fieldsAfterAdd.size());
    }

    @Test
    public void addNullDescriptorFieldTest() {
        try {
            descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME, DESCRIPTOR_TYPE);
            final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
            descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor field cannot be null", e.getMessage());
        }
    }

    @Test
    public void addDescriptorFieldThatAlreadyExistsTest() {
        try {
            final String field1Key = "field1";
            final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
            descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
            final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

            descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), field1);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("This field cannot be added because it already exists", e.getMessage());
        }
    }

    @Test
    public void updateDescriptorFieldTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

        final List<DescriptorFieldModel> initialFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME);
        assertEquals(1, initialFields.size());

        final String newKey = "newFieldKey";
        descriptorAccessor.updateDescriptorFieldKey(registeredDescriptorModel.getId(), field1Key, newKey);

    }

    @Test
    public void updateDescriptorFieldWithEmptyTest() throws AlertDatabaseConstraintException {
        updateDescriptorFieldWithEmptyTestHelper(null);
        updateDescriptorFieldWithEmptyTestHelper("");
    }

    @Test
    public void addUpdateDeleteDescriptorFieldWithNullDescriptorIdTest() {
        try {
            descriptorAccessor.addDescriptorField(null, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
        try {
            descriptorAccessor.updateDescriptorFieldKey(null, null, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
        try {
            descriptorAccessor.deleteDescriptorField(null, Mockito.mock(DescriptorFieldModel.class));
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
        try {
            descriptorAccessor.deleteDescriptorField(null, (String) null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
    }

    @Test
    public void deleteDescriptorFieldByModelTest() throws AlertDatabaseConstraintException {
        final DescriptorFieldModel field1 = new DescriptorFieldModel("field1", Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        assertEquals(1, descriptorFieldRepository.findAll().size());

        descriptorAccessor.deleteDescriptorField(registeredDescriptorModel.getId(), field1);
        assertEquals(0, descriptorFieldRepository.findAll().size());
    }

    @Test
    public void deleteNullDescriptorFieldTest() {
        try {
            descriptorAccessor.deleteDescriptorField(null, (DescriptorFieldModel) null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Cannot delete a null object from the database", e.getMessage());
        }
    }

    @Test
    public void deleteDescriptorFieldByKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DescriptorFieldModel field1 = new DescriptorFieldModel("field1", Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        assertEquals(1, descriptorFieldRepository.findAll().size());

        descriptorAccessor.deleteDescriptorField(registeredDescriptorModel.getId(), field1Key);
        assertEquals(0, descriptorFieldRepository.findAll().size());
    }

    @Test
    public void deleteDescriptorFieldWithEmptyKeyTest() throws AlertDatabaseConstraintException {
        final DescriptorFieldModel field1 = new DescriptorFieldModel("field1", Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        deleteDescriptorFieldWithEmptyKeyTestHelper(registeredDescriptorModel.getId(), null);
        deleteDescriptorFieldWithEmptyKeyTestHelper(registeredDescriptorModel.getId(), "");
    }

    @Test
    public void deleteDescriptorFieldWithInvalidKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String invalidFieldKey = " -- INVALID FIELD KEY -- ";
        final DescriptorFieldModel field1 = new DescriptorFieldModel(field1Key, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

        final boolean validResult = descriptorAccessor.deleteDescriptorField(registeredDescriptorModel.getId(), field1Key);
        assertTrue(validResult);

        final boolean invalidResult = descriptorAccessor.deleteDescriptorField(registeredDescriptorModel.getId(), invalidFieldKey);
        assertFalse(invalidResult);
    }

    private void getRegisteredDescriptorByNameTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.registerDescriptorWithoutFields(descriptorName, DESCRIPTOR_TYPE);
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

    private void unregisterDescriptorTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.unregisterDescriptor(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void getFieldsForDescriptorWithEmptyArgsTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.getFieldsForDescriptor(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void updateDescriptorFieldWithEmptyTestHelper(final String keyString) throws AlertDatabaseConstraintException {
        final String fieldKey = "key";
        final DescriptorFieldModel field = new DescriptorFieldModel(fieldKey, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(field));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        try {
            descriptorAccessor.updateDescriptorFieldKey(registeredDescriptorModel.getId(), keyString, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The old field key cannot be empty", e.getMessage());
        }
        try {
            descriptorAccessor.updateDescriptorFieldKey(registeredDescriptorModel.getId(), fieldKey, keyString);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The new field key cannot be empty", e.getMessage());
        }
    }

    private void deleteDescriptorFieldWithEmptyKeyTestHelper(final Long validId, final String key) {
        try {
            descriptorAccessor.deleteDescriptorField(validId, key);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The field key cannot be empty", e.getMessage());
        }
    }
}
