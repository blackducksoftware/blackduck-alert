/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.util.function.Predicate;

import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class EnvironmentVariableMockingUtil {
    public static void addEnvironmentVariableValueToMock(Environment mockedEnvironment, Predicate<String> hasEnvVarCheck, String propertyKey, String value) {
        if (Mockito.mockingDetails(mockedEnvironment).isMock()) {
            Mockito.doAnswer((invocation -> {
                String environmentVariableName = invocation.getArgument(0);
                return hasEnvVarCheck.test(environmentVariableName);
            })).when(mockedEnvironment).containsProperty(Mockito.anyString());
            Mockito.when(mockedEnvironment.getProperty(propertyKey)).thenReturn(value);
        }
    }

    private EnvironmentVariableMockingUtil() {
        // prevent construction
    }
}
