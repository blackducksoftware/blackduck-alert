package com.synopsys.integration.alert.test.common;

import java.util.function.Predicate;

import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class EnvironmentVariableUtil {
    public static void addEnvironmentVariableValueToMock(Environment mockedEnvironment, Predicate<String> hasEnvVarCheck, String propertyKey, String value) {
        if (Mockito.mockingDetails(mockedEnvironment).isMock()) {
            Mockito.doAnswer((invocation -> {
                String environmentVariableName = invocation.getArgument(0);
                return hasEnvVarCheck.test(environmentVariableName);
            })).when(mockedEnvironment).containsProperty(Mockito.anyString());
            Mockito.when(mockedEnvironment.getProperty(propertyKey)).thenReturn(value);
        }
    }

    private EnvironmentVariableUtil() {
        // prevent construction
    }
}
