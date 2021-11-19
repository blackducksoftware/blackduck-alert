package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class EnvironmentVariableProcessorTest {

    @Test
    public void testUpdatingHandler() {
        Environment environment = Mockito.mock(Environment.class);
        for (String variableName : EnvironmentTestHandler.VARIABLE_NAMES) {
            Mockito.when(environment.containsProperty(Mockito.eq(variableName))).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(Mockito.eq(variableName))).thenReturn(EnvironmentTestHandler.DEFAULT_VALUE);
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentTestHandler handler = new EnvironmentTestHandler(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertTrue(handler.hasUpdateOccurred());
        Properties updatedProperties = handler.getUpdatedProperties().orElseThrow(() -> new AssertionError("Properties should exist"));

        assertFalse(updatedProperties.isEmpty());
        assertTrue(EnvironmentTestHandler.VARIABLE_NAMES.containsAll(updatedProperties.stringPropertyNames()));
    }

    @Test
    public void testMissingEnvironmentVariablesHandler() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentTestHandler handler = new EnvironmentTestHandler(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertFalse(handler.hasUpdateOccurred());
        assertTrue(handler.getUpdatedProperties().stream()
            .allMatch(Properties::isEmpty));
    }

    private static class EnvironmentTestHandler implements EnvironmentVariableHandler {
        public static final String HANDLER_NAME = "Updating Handler";
        protected static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_DESCRIPTORTYPE_DESCRIPTORNAME_";
        public static final Set<String> VARIABLE_NAMES = Set.of(
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_1",
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_2");
        public static final String DEFAULT_VALUE = "environmentPropertyValue";

        private boolean updateOccurred = false;
        private EnvironmentVariableUtility environmentVariableUtility;
        private Properties updatedProperties;

        public EnvironmentTestHandler(EnvironmentVariableUtility environmentVariableUtility) {
            this.environmentVariableUtility = environmentVariableUtility;
        }

        public boolean hasUpdateOccurred() {
            return updateOccurred;
        }

        public Optional<Properties> getUpdatedProperties() {
            return Optional.ofNullable(updatedProperties);
        }

        @Override
        public String getName() {
            return HANDLER_NAME;
        }

        @Override
        public Set<String> getVariableNames() {
            return VARIABLE_NAMES;
        }

        @Override
        public Properties updateFromEnvironment() {
            Properties properties = new Properties();
            for (String variableName : VARIABLE_NAMES) {
                if (environmentVariableUtility.hasEnvironmentValue(variableName)) {
                    properties.put(variableName, environmentVariableUtility.getEnvironmentValue(variableName));
                    updateOccurred = true;
                }
            }
            updatedProperties = properties;
            return properties;
        }
    }
}
