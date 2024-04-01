package com.synopsys.integration.alert.api.environment;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentVariableProcessorTest {
    @Test
    void testUpdatingHandler() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        for (String variableName : EnvironmentTestHandler.VARIABLE_NAMES) {
            mockEnvironment.setProperty(variableName, EnvironmentTestHandler.DEFAULT_VALUE);
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        EnvironmentTestHandler handler = new EnvironmentTestHandler(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertTrue(handler.hasUpdateOccurred());
        assertTrue(handler.hasSaveOccurred());
        EnvironmentProcessingResult result = handler.getUpdatedProperties().orElseThrow(() -> new AssertionError("Properties should exist"));

        assertTrue(result.hasValues());
        assertTrue(EnvironmentTestHandler.VARIABLE_NAMES.containsAll(result.getVariableNames()));
    }

    @Test
    void testMissingEnvironmentVariablesHandler() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentTestHandler handler = new EnvironmentTestHandler(environmentVariableUtility);
        EnvironmentVariableProcessor processor = new EnvironmentVariableProcessor(List.of(handler));
        processor.updateConfigurations();
        assertFalse(handler.hasUpdateOccurred());
        assertFalse(handler.hasSaveOccurred());
        assertFalse(handler.getUpdatedProperties().isPresent());
    }

    private static class TestModel implements Obfuscated<TestModel> {
        @Override
        public TestModel obfuscate() {
            return null;
        }
    }

    private static class EnvironmentTestHandler extends EnvironmentVariableHandler<TestModel> {
        public static final String HANDLER_NAME = "Updating Handler";
        protected static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_DESCRIPTORTYPE_DESCRIPTORNAME_";
        public static final Set<String> VARIABLE_NAMES = Set.of(
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_1",
            ENVIRONMENT_VARIABLE_PREFIX + "PROPERTY_NAME_2");
        public static final String DEFAULT_VALUE = "environmentPropertyValue";

        private boolean updateOccurred = false;
        private boolean saveOccurred = false;
        private final EnvironmentVariableUtility environmentVariableUtility;
        private EnvironmentProcessingResult updatedProperties;

        protected EnvironmentTestHandler(EnvironmentVariableUtility environmentVariableUtility) {
            super(HANDLER_NAME, VARIABLE_NAMES, environmentVariableUtility);
            this.environmentVariableUtility = environmentVariableUtility;
        }

        @Override
        protected Boolean configurationMissingCheck() {
            return true;
        }

        @Override
        protected TestModel configureModel() {
            return new TestModel();
        }

        @Override
        protected ValidationResponseModel validateConfiguration(TestModel configModel) {
            return ValidationResponseModel.success();
        }

        @Override
        protected EnvironmentProcessingResult buildProcessingResult(TestModel obfuscatedConfigModel) {
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

        @Override
        protected void saveConfiguration(TestModel configModel, EnvironmentProcessingResult processingResult) {
            saveOccurred = true;
        }

        public boolean hasUpdateOccurred() {
            return updateOccurred;
        }

        public boolean hasSaveOccurred() {
            return saveOccurred;
        }

        public Optional<EnvironmentProcessingResult> getUpdatedProperties() {
            return Optional.ofNullable(updatedProperties);
        }
    }
}
