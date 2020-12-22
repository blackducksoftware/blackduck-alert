package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.api.mock.MockProviderTaskPropertiesRepository;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

public class DefaultProviderTaskPropertiesAccessorTest {
    private final Long providerConfigId = 1L;
    private final String taskName = "taskName-test";
    private final String propertyName = "propertyName-test";
    private final String value = "value-test";

    private final ProviderTaskPropertiesRepository providerTaskPropertiesRepository = new MockProviderTaskPropertiesRepository(providerConfigId, taskName, propertyName, value);

    @Test
    public void getTaskPropertyTest() {
        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyValue = providerTaskPropertiesAccessor.getTaskProperty(taskName, propertyName);

        assertTrue(taskPropertyValue.isPresent());
        assertEquals(value, taskPropertyValue.get());
    }

    @Test
    public void getTaskPropertyEmptyTest() {
        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyOptionalEmpty = providerTaskPropertiesAccessor.getTaskProperty("invalidTaskName", "invalidPropertyKey");
        Optional<String> taskPropertyValueEmpty = providerTaskPropertiesAccessor.getTaskProperty("", "");

        assertFalse(taskPropertyOptionalEmpty.isPresent());
        assertFalse(taskPropertyValueEmpty.isPresent());
    }

    @Test
    public void setTaskPropertyTest() {
        final Long newConfigId = 2L;
        final String newTaskName = "taskName-new";
        final String newPropertyName = "propertyName-new";
        final String newValue = "value-new";

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        providerTaskPropertiesAccessor.setTaskProperty(newConfigId, newTaskName, newPropertyName, newValue);

        Optional<ProviderTaskPropertiesEntity> providerTaskPropertiesEntityOptional = providerTaskPropertiesRepository.findByTaskNameAndPropertyName(newTaskName, newPropertyName);

        assertTrue(providerTaskPropertiesEntityOptional.isPresent());
        ProviderTaskPropertiesEntity providerTaskPropertiesEntity = providerTaskPropertiesEntityOptional.get();
        assertEquals(newConfigId, providerTaskPropertiesEntity.getProviderConfigId());
        assertEquals(newTaskName, providerTaskPropertiesEntity.getTaskName());
        assertEquals(newPropertyName, providerTaskPropertiesEntity.getPropertyName());
        assertEquals(newValue, providerTaskPropertiesEntity.getValue());
    }

}
