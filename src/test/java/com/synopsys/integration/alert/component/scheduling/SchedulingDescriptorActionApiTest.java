package com.synopsys.integration.alert.component.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.exception.IntegrationException;

public class SchedulingDescriptorActionApiTest {
    private static final ConfigurationFieldModel FIELD_HOUR_OF_DAY = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY);
    private static final ConfigurationFieldModel FIELD_PURGE_FREQUENCY = ConfigurationFieldModel.create(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS);
    private static final Map<String, ConfigurationFieldModel> FIELD_MAP = MockConfigurationModelFactory.mapFieldKeyToFields(Set.of(FIELD_HOUR_OF_DAY, FIELD_PURGE_FREQUENCY));
    private static final FieldAccessor FIELD_ACCESSOR = new FieldAccessor(FIELD_MAP);

    @AfterEach
    public void cleanup() {
        FIELD_HOUR_OF_DAY.setFieldValues(null);
        FIELD_PURGE_FREQUENCY.setFieldValues(null);
    }

    @Test
    public void validateConfigWithNoErrorsTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValue("1");
        FIELD_PURGE_FREQUENCY.setFieldValue("1");
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals(null, fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));
        assertEquals(null, fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenNullTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValues(null);
        FIELD_PURGE_FREQUENCY.setFieldValues(null);
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals("Must be a number between 0 and 23", fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));
        assertEquals("Must be a number between 1 and 7", fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenEmptyStringTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValue("");
        FIELD_PURGE_FREQUENCY.setFieldValue("");
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals("Must be a number between 0 and 23", fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));
        assertEquals("Must be a number between 1 and 7", fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenValuesNotNumericTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValue("not a number");
        FIELD_PURGE_FREQUENCY.setFieldValue("not a number");
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals("Must be a number between 0 and 23", fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));
        assertEquals("Must be a number between 1 and 7", fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenHourOutOfRangeTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValue("-1");
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals("Must be a number between 0 and 23", fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));

        fieldErrors.put(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY, null);
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        FIELD_HOUR_OF_DAY.setFieldValue("24");
        assertEquals("Must be a number between 0 and 23", fieldErrors.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY));
    }

    @Test
    public void validateConfigHasErrorWhenPurgeFrequencyOutOfRangeTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();

        FIELD_HOUR_OF_DAY.setFieldValue("0");
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        assertEquals("Must be a number between 1 and 7", fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));

        fieldErrors.put(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS, null);
        actionApi.validateConfig(FIELD_ACCESSOR, fieldErrors);
        FIELD_HOUR_OF_DAY.setFieldValue("8");
        assertEquals("Must be a number between 1 and 7", fieldErrors.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void testConfigTest() {
        final SchedulingDescriptorActionApi actionApi = new SchedulingDescriptorActionApi();
        try {
            actionApi.testConfig(null);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
            assertEquals("Should not be implemented", e.getMessage());
        }
    }
}
