package com.synopsys.integration.alert.test.common.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopys.integration.alert.test.common.environment.TestEnvironmentHandlerWrapper;

public class TestEnvironmentHandlerWrapperTest {
    private String handlerName = "My Test Handler";
    private Set<String> variableNames = Set.of("ALERT_DESCRIPTORTYPE_NAME_PROPERTY_KEY_1", "ALERT_DESCRIPTORTYPE_NAME_PROPERTY_KEY_2");

    @Test
    public void testHandlerWrapper() {
        EnvironmentVariableHandler handler = new EnvironmentVariableHandler() {
            @Override
            public String getName() {
                return handlerName;
            }

            @Override
            public Set<String> getVariableNames() {
                return variableNames;
            }

            @Override
            public Properties updateFromEnvironment() {
                Properties properties = new Properties();
                for (String variableName : variableNames) {
                    properties.put(variableName, "A value");
                }
                return properties;
            }
        };
        TestEnvironmentHandlerWrapper handlerWrapper = new TestEnvironmentHandlerWrapper(handler);
        handlerWrapper.updateFromEnvironment();
        assertEquals(handlerName, handlerWrapper.getName());
        assertEquals(variableNames, handlerWrapper.getVariableNames());
        assertTrue(handlerWrapper.hasUpdateOccurred());
        assertTrue(handlerWrapper.getUpdatedProperties().stream()
            .allMatch((properties) -> variableNames.containsAll(properties.stringPropertyNames())));
    }

    @Test
    public void testHandlerWrapperUpdateDidNotOccur() {
        EnvironmentVariableHandler handler = new EnvironmentVariableHandler() {
            @Override
            public String getName() {
                return handlerName;
            }

            @Override
            public Set<String> getVariableNames() {
                return variableNames;
            }

            @Override
            public Properties updateFromEnvironment() {
                Properties properties = new Properties();
                return properties;
            }
        };
        TestEnvironmentHandlerWrapper handlerWrapper = new TestEnvironmentHandlerWrapper(handler);
        assertEquals(handlerName, handlerWrapper.getName());
        assertEquals(variableNames, handlerWrapper.getVariableNames());
        assertFalse(handlerWrapper.hasUpdateOccurred());
        assertTrue(handlerWrapper.getUpdatedProperties().stream()
            .allMatch(Properties::isEmpty));
    }
}
