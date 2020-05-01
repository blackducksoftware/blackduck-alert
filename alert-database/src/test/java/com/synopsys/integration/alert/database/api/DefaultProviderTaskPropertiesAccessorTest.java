package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.mock.MockProviderTaskPropertiesRepository;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

public class DefaultProviderTaskPropertiesAccessorTest {
    private final Long providerConfigId = 1L;
    private final String taskName = "taskName-test";
    private final String propertyName = "propertyName-test";
    private final String value = "value-test";

    @Test
    public void getTaskPropertyTest() {
        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = new MockProviderTaskPropertiesRepository(providerConfigId, taskName, propertyName, value);

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyValue = providerTaskPropertiesAccessor.getTaskProperty(taskName, propertyName);

        assertTrue(taskPropertyValue.isPresent());
        assertEquals(value, taskPropertyValue.get());
    }
    /*
    @Test
    public void getTaskPropertyTest() {
        ProviderTaskPropertiesEntity providerTaskPropertiesEntity = new ProviderTaskPropertiesEntity(providerConfigId, taskName, propertyName, value);

        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = Mockito.mock(ProviderTaskPropertiesRepository.class);

        Mockito.when(providerTaskPropertiesRepository.findByTaskNameAndPropertyName(Mockito.any(), Mockito.any())).thenReturn(Optional.of(providerTaskPropertiesEntity));

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyValue = providerTaskPropertiesAccessor.getTaskProperty(taskName, propertyName);

        assertTrue(taskPropertyValue.isPresent());
        assertEquals(value, taskPropertyValue.get());
    }*/

    @Test
    public void getTaskPropertyEmptyTest() {
        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = new MockProviderTaskPropertiesRepository(providerConfigId, taskName, propertyName, value);

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyOptionalEmpty = providerTaskPropertiesAccessor.getTaskProperty("invalidTaskName", "invalidPropertyKey");
        Optional<String> taskPropertyValueEmpty = providerTaskPropertiesAccessor.getTaskProperty("", "");

        assertFalse(taskPropertyOptionalEmpty.isPresent());
        assertFalse(taskPropertyValueEmpty.isPresent());
    }
    /*
    @Test
    public void getTaskPropertyEmptyTest() {
        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = Mockito.mock(ProviderTaskPropertiesRepository.class);

        Mockito.when(providerTaskPropertiesRepository.findByTaskNameAndPropertyName(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        Optional<String> taskPropertyOptionalEmpty = providerTaskPropertiesAccessor.getTaskProperty(taskName, propertyName);
        Optional<String> taskPropertyValueEmpty = providerTaskPropertiesAccessor.getTaskProperty("", "");

        assertFalse(taskPropertyOptionalEmpty.isPresent());
        assertFalse(taskPropertyValueEmpty.isPresent());
    }

     */

    @Test
    public void setTaskPropertyTest() throws Exception {
        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = Mockito.mock(ProviderTaskPropertiesRepository.class);

        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);
        providerTaskPropertiesAccessor.setTaskProperty(providerConfigId, taskName, propertyName, value);

        Mockito.verify(providerTaskPropertiesRepository).save(Mockito.any());
    }

    @Test
    public void setTaskPropertyExceptionTest() throws Exception {
        ProviderTaskPropertiesRepository providerTaskPropertiesRepository = Mockito.mock(ProviderTaskPropertiesRepository.class);
        DefaultProviderTaskPropertiesAccessor providerTaskPropertiesAccessor = new DefaultProviderTaskPropertiesAccessor(providerTaskPropertiesRepository);

        try {
            providerTaskPropertiesAccessor.setTaskProperty(null, "", "", "");
            fail();
        } catch (AlertDatabaseConstraintException e) {
        }
    }
}
