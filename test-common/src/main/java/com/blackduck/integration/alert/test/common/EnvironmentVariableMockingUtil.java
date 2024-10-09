package com.blackduck.integration.alert.test.common;

import java.util.function.Predicate;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class EnvironmentVariableMockingUtil {
    public static void addEnvironmentVariableValueToMock(Environment mockedEnvironment, Predicate<String> hasEnvVarCheck, String propertyKey, String value) {
        if (Mockito.mockingDetails(mockedEnvironment).isMock()) {
            Mockito.doAnswer((invocation -> {
                String environmentVariableName = invocation.getArgument(0);
                return hasEnvVarCheck.test(environmentVariableName);
            })).when(mockedEnvironment).containsProperty(ArgumentMatchers.anyString());
            Mockito.when(mockedEnvironment.getProperty(propertyKey)).thenReturn(value);
        }
    }

    private EnvironmentVariableMockingUtil() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }
}
