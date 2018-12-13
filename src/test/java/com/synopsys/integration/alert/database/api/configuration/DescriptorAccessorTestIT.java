package com.synopsys.integration.alert.database.api.configuration;

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
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;

public class DescriptorAccessorTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private FieldContextRepository fieldContextRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;

    private DescriptorAccessor descriptorAccessor;

    @Before
    public void init() {
        descriptorAccessor = new DescriptorAccessor(registeredDescriptorRepository, descriptorFieldRepository, definedFieldRepository, fieldContextRepository, configContextRepository);
    }

    @After
    public void cleanup() {
        registeredDescriptorRepository.deleteAllInBatch();
        definedFieldRepository.deleteAllInBatch();
        configContextRepository.deleteAllInBatch();
        // No need to delete relations as they will be deleted by the tables they reference (CASCADE)
    }

    @Test
    public void registerAndGetAllDescriptorsTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME + "2");
        assertEquals(2, registeredDescriptorRepository.findAll().size());

        final List<RegisteredDescriptorModel> registeredDescriptors = descriptorAccessor.getRegisteredDescriptors();
        assertEquals(2, registeredDescriptors.size());
    }

    @Test
    public void registerAndGetDescriptorTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor
                                                                            .getRegisteredDescriptorByName(DESCRIPTOR_NAME)
                                                                            .orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        assertNotNull(registeredDescriptorModel.getId());
        assertEquals(DESCRIPTOR_NAME, registeredDescriptorModel.getName());
    }

    @Test
    public void registerDescriptorWithFieldsTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String field2Key = "field2";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        final DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);

        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1, field2));
        assertEquals(1, registeredDescriptorRepository.findAll().size());
        assertEquals(2, definedFieldRepository.findAll().size());
    }

    @Test
    public void registerDescriptorThatAlreadyExistsTest() throws AlertDatabaseConstraintException {
        final boolean initialResult = descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
        assertTrue(initialResult);
        final boolean reAddResult = descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
        assertFalse(reAddResult);
    }

    @Test
    public void getRegisteredDescriptorByNameWithEmptyNameTest() {
        getRegisteredDescriptorByNameTestHelper(null);
        getRegisteredDescriptorByNameTestHelper("");
    }

    @Test
    public void unregisterDescriptorTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
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
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        final DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1, field2));
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
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1, field2));
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

    @Test
    public void addDescriptorFieldTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

        final List<DefinedFieldModel> initialFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME, ConfigContextEnum.DISTRIBUTION);
        assertEquals(1, initialFields.size());

        final String field2Key = "field2";
        final DefinedFieldModel field2 = new DefinedFieldModel(field2Key, ConfigContextEnum.DISTRIBUTION, Boolean.TRUE);
        descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), field2);

        final List<DefinedFieldModel> fieldsAfterAdd = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME, ConfigContextEnum.DISTRIBUTION);
        assertEquals(2, fieldsAfterAdd.size());
    }

    @Test
    public void addNullDescriptorFieldTest() {
        try {
            descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
            final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
            descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor field cannot be null", e.getMessage());
        }
    }

    @Test
    public void addDescriptorFieldWithNullDescriptorIdTest() {
        try {
            descriptorAccessor.addDescriptorField(null, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
    }

    @Test
    public void updateFieldKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        assertEquals(1, registeredDescriptorRepository.findAll().size());
        assertEquals(1, descriptorFieldRepository.findAll().size());
        assertEquals(1, definedFieldRepository.findAll().size());

        final List<DefinedFieldModel> initialFields = descriptorAccessor.getFieldsForDescriptor(DESCRIPTOR_NAME, ConfigContextEnum.DISTRIBUTION);
        assertEquals(1, initialFields.size());

        final String newKey = "newFieldKey";
        descriptorAccessor.updateDefinedFieldKey(field1Key, newKey);

    }

    @Test
    public void updateFieldKeyWithEmptyTest() throws AlertDatabaseConstraintException {
        updateDefinedFieldKeyWithEmptyTestHelper(null);
        updateDefinedFieldKeyWithEmptyTestHelper("");
    }

    @Test
    public void removeDescriptorFieldTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        assertEquals(1, registeredDescriptorRepository.findAll().size());
        assertEquals(1, descriptorFieldRepository.findAll().size());
        assertEquals(1, definedFieldRepository.findAll().size());

        final RegisteredDescriptorModel descriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        descriptorAccessor.removeDescriptorField(descriptorModel.getId(), field1);
        assertEquals(1, registeredDescriptorRepository.findAll().size());
        assertEquals(0, descriptorFieldRepository.findAll().size());
        assertEquals(0, definedFieldRepository.findAll().size());
    }

    @Test
    public void removeDescriptorFieldWithNullIdTest() {
        try {
            descriptorAccessor.removeDescriptorField(null, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor id cannot be null", e.getMessage());
        }
    }

    @Test
    public void removeNullDescriptorFieldTest() {
        try {
            descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
            final RegisteredDescriptorModel descriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
            descriptorAccessor.removeDescriptorField(descriptorModel.getId(), null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The descriptor field cannot be null", e.getMessage());
        }
    }

    @Test
    public void removeDescriptorFieldWithEmptyKeyTest() throws AlertDatabaseConstraintException {
        descriptorAccessor.registerDescriptorWithoutFields(DESCRIPTOR_NAME);
        final RegisteredDescriptorModel descriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        removeDescriptorFieldWithEmptyKeyTestHelper(descriptorModel.getId(), new DefinedFieldModel(null, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE));
        removeDescriptorFieldWithEmptyKeyTestHelper(descriptorModel.getId(), new DefinedFieldModel("", ConfigContextEnum.DISTRIBUTION, Boolean.FALSE));
    }

    @Test
    public void removeDescriptorFieldWithInvalidKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String invalidFieldKey = " -- INVALID FIELD KEY -- ";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        final DefinedFieldModel invalidField = new DefinedFieldModel(invalidFieldKey, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        final RegisteredDescriptorModel descriptorModel = descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));

        final boolean validResult = descriptorAccessor.removeDescriptorField(descriptorModel.getId(), field1);
        assertTrue(validResult);

        final boolean invalidResult = descriptorAccessor.removeDescriptorField(descriptorModel.getId(), invalidField);
        assertFalse(invalidResult);
    }

    @Test
    public void deleteDescriptorFieldByModelTest() throws AlertDatabaseConstraintException {
        final DefinedFieldModel field1 = new DefinedFieldModel("field1", ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        assertEquals(1, definedFieldRepository.findAll().size());

        descriptorAccessor.deleteDefinedField(field1);
        assertEquals(0, definedFieldRepository.findAll().size());
    }

    @Test
    public void deleteNullDescriptorFieldTest() {
        try {
            descriptorAccessor.deleteDefinedField((DefinedFieldModel) null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Cannot delete a null object from the database", e.getMessage());
        }
    }

    @Test
    public void deleteDescriptorFieldByKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final DefinedFieldModel field1 = new DefinedFieldModel("field1", ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));
        assertEquals(1, definedFieldRepository.findAll().size());

        descriptorAccessor.deleteDefinedField(field1Key);
        assertEquals(0, definedFieldRepository.findAll().size());
    }

    @Test
    public void deleteDescriptorFieldWithEmptyKeyTest() throws AlertDatabaseConstraintException {
        final DefinedFieldModel field1 = new DefinedFieldModel("field1", ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));

        deleteDefinedFieldWithEmptyKeyTestHelper(null);
        deleteDefinedFieldWithEmptyKeyTestHelper("");
    }

    @Test
    public void deleteDescriptorFieldWithInvalidKeyTest() throws AlertDatabaseConstraintException {
        final String field1Key = "field1";
        final String invalidFieldKey = " -- INVALID FIELD KEY -- ";
        final DefinedFieldModel field1 = new DefinedFieldModel(field1Key, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field1));

        final boolean validResult = descriptorAccessor.deleteDefinedField(field1Key);
        assertTrue(validResult);

        final boolean invalidResult = descriptorAccessor.deleteDefinedField(invalidFieldKey);
        assertFalse(invalidResult);
    }

    private void getRegisteredDescriptorByNameTestHelper(final String descriptorName) {
        try {
            descriptorAccessor.registerDescriptorWithoutFields(descriptorName);
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
            descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.DISTRIBUTION);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void updateDefinedFieldKeyWithEmptyTestHelper(final String keyString) throws AlertDatabaseConstraintException {
        final String fieldKey = "key";
        final DefinedFieldModel field = new DefinedFieldModel(fieldKey, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, Arrays.asList(field));
        descriptorAccessor.getRegisteredDescriptorByName(DESCRIPTOR_NAME).orElseThrow(() -> new AlertDatabaseConstraintException("This descriptor should exist"));
        try {
            descriptorAccessor.updateDefinedFieldKey(keyString, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The old field key cannot be empty", e.getMessage());
        }
        try {
            descriptorAccessor.updateDefinedFieldKey(fieldKey, keyString);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The new field key cannot be empty", e.getMessage());
        }
    }

    private void deleteDefinedFieldWithEmptyKeyTestHelper(final String key) {
        try {
            descriptorAccessor.deleteDefinedField(key);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The field key cannot be empty", e.getMessage());
        }
    }

    private void removeDescriptorFieldWithEmptyKeyTestHelper(final Long descritorId, final DefinedFieldModel fieldModel) {
        try {
            descriptorAccessor.removeDescriptorField(descritorId, fieldModel);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The field key cannot be empty", e.getMessage());
        }
    }
}
