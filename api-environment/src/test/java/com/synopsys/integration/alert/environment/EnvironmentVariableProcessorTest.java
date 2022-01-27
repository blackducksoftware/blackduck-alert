package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

class EnvironmentVariableProcessorTest {

    @Test
    void testUpdatingHandler() {
        Environment environment = Mockito.mock(Environment.class);
        for (String variableName : EnvironmentTestHandlerFactory.VARIABLE_NAMES) {
            Mockito.when(environment.containsProperty(variableName)).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(variableName)).thenReturn(EnvironmentTestHandlerFactory.DEFAULT_VALUE);
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentTestHandlerFactory handler = new EnvironmentTestHandlerFactory(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertTrue(handler.hasUpdateOccurred());
        EnvironmentProcessingResult result = handler.getUpdatedProperties().orElseThrow(() -> new AssertionError("Properties should exist"));

        assertTrue(result.hasValues());
        assertTrue(EnvironmentTestHandlerFactory.VARIABLE_NAMES.containsAll(result.getVariableNames()));
    }

    @Test
    void testMissingEnvironmentVariablesHandler() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentTestHandlerFactory handler = new EnvironmentTestHandlerFactory(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertFalse(handler.hasUpdateOccurred());
        assertFalse(handler.getUpdatedProperties().stream()
            .allMatch(EnvironmentProcessingResult::hasValues));
    }

    private static class EnvironmentTestHandlerFactory implements EnvironmentVariableHandlerFactory {
        public static final String HANDLER_NAME = "Updating Handler";
        protected static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_DESCRIPTORTYPE_DESCRIPTORNAME_";
        public static final Set<String> VARIABLE_NAMES = Set.of(
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_1",
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_2");
        public static final String DEFAULT_VALUE = "environmentPropertyValue";

        private boolean updateOccurred = false;
        private EnvironmentVariableUtility environmentVariableUtility;
        private EnvironmentProcessingResult updatedProperties;

        public EnvironmentTestHandlerFactory(EnvironmentVariableUtility environmentVariableUtility) {
            this.environmentVariableUtility = environmentVariableUtility;
        }

        @Override
        public EnvironmentVariableHandler build() {
            return new EnvironmentVariableHandler(HANDLER_NAME, VARIABLE_NAMES, Boolean.TRUE::booleanValue, this::updateFromEnvironment);
        }

        public boolean hasUpdateOccurred() {
            return updateOccurred;
        }

        public Optional<EnvironmentProcessingResult> getUpdatedProperties() {
            return Optional.ofNullable(updatedProperties);
        }

        private EnvironmentProcessingResult updateFromEnvironment() {
            EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
            for (String variableName : VARIABLE_NAMES) {
                if (environmentVariableUtility.hasEnvironmentValue(variableName)) {
                    String value = environmentVariableUtility.getEnvironmentValue(variableName).orElse(StringUtils.EMPTY);
                    builder.addVariableValue(variableName, value);
                    updateOccurred = true;
                }
            }
            updatedProperties = builder.build();
            return updatedProperties;
        }
    }
}
